package tn.tp.atelierandroid

class GestionPlaces(
    private val participants: MutableList<Participant>,
    private val capacite: Int = 2
) {
    private var prochainTicket = 1

    // Compte des places libres dans un groupe
    fun placesLibres(groupe: String): Int {
        var confirmes = 0
        for (p in participants) {
            if (p.groupe == groupe && p.ticketAttente == null) {
                confirmes++
            }
        }
        return (capacite - confirmes).coerceAtLeast(0)
    }

    // Statut lisible
    fun statut(p: Participant): String {
        return if (p.ticketAttente == null) "Confirmé" else "Attente #${p.ticketAttente}"
    }

    // Inscription avec gestion des doublons et de la file
    fun inscrire(candidat: Participant): Boolean {
        // Refus des doublons (même nom + prénom, même en attente)
        val doublon = participants.any {
            it.nom.equals(candidat.nom, ignoreCase = true) &&
            it.prenom.equals(candidat.prenom, ignoreCase = true)
        }
        if (doublon) return false

        if (placesLibres(candidat.groupe) > 0) {
            candidat.ticketAttente = null
        } else {
            candidat.ticketAttente = prochainTicket++
        }
        participants.add(candidat)
        return true
    }

    // Suppression avec promotion automatique si confirmé
    fun supprimer(p: Participant): Boolean {
        if (!participants.remove(p)) return false
        if (p.ticketAttente == null) {
            promouvoirPremier(p.groupe)
        }
        return true
    }

    // Transfert entre groupes
    fun changerGroupe(p: Participant, nouveauGroupe: String): Boolean {
        if (!participants.contains(p) || p.groupe == nouveauGroupe) return false

        val ancien = p.groupe
        val etaitConfirme = p.ticketAttente == null
        val destinationLibre = placesLibres(nouveauGroupe) > 0

        p.groupe = nouveauGroupe
        p.ticketAttente = if (destinationLibre) null else prochainTicket++

        if (etaitConfirme) {
            promouvoirPremier(ancien)
        }
        return true
    }

    // Promotion du premier ticket d'un groupe
    private fun promouvoirPremier(groupe: String): Participant? {
        if (placesLibres(groupe) == 0) return null
        var premier: Participant? = null
        for (p in participants) {
            val numero = p.ticketAttente ?: continue
            if (p.groupe == groupe && (premier == null || numero < premier!!.ticketAttente!!)) {
                premier = p
            }
        }
        premier?.ticketAttente = null
        return premier
    }

    // Affichage des capacités globales
    fun capacitesGlobales(): String {
        return "Places libres : G1=${placesLibres("G1")} / G2=${placesLibres("G2")} / G3=${placesLibres("G3")}"
    }

    // Affichage de la file d'attente
    fun fileAttenteTexte(): String {
        val attente = participants.filter { it.ticketAttente != null }
            .sortedWith(compareBy({ it.groupe }, { it.ticketAttente }))
        if (attente.isEmpty()) return "Aucune attente"
        val sb = StringBuilder("Attente : ")
        attente.forEachIndexed { i, p ->
            if (i > 0) sb.append(", ")
            sb.append("${p.prenom} ${p.nom} (${p.groupe} #${p.ticketAttente})")
        }
        return sb.toString()
    }
}