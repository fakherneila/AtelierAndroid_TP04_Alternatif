package tn.tp.atelierandroid

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val participants = mutableListOf<Participant>()
    private lateinit var gestion: GestionPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestion = GestionPlaces(participants)

        // Récupération de tous les composants
        val etNom = findViewById<EditText>(R.id.etNom)
        val etPrenom = findViewById<EditText>(R.id.etPrenom)
        val etAge = findViewById<EditText>(R.id.etAge)
        val spGroupe = findViewById<Spinner>(R.id.spGroupe)
        val rgSupport = findViewById<RadioGroup>(R.id.rgSupport)
        val cbConfirmation = findViewById<CheckBox>(R.id.cbConfirmation)

        val btnAfficher = findViewById<Button>(R.id.btnAfficher)
        val btnEffacer = findViewById<Button>(R.id.btnEffacer)
        val btnEnregistrer = findViewById<Button>(R.id.btnEnregistrer)
        val btnListe = findViewById<Button>(R.id.btnListe)
        val btnRechercher = findViewById<Button>(R.id.btnRechercher)
        val btnSupprimer = findViewById<Button>(R.id.btnSupprimer)
        val btnTransferer = findViewById<Button>(R.id.btnTransferer)

        val tvResultat = findViewById<TextView>(R.id.tvResultat)
        val tvCompteur = findViewById<TextView>(R.id.tvCompteur)
        val tvCapacities = findViewById<TextView>(R.id.tvCapacities)
        val tvFileAttente = findViewById<TextView>(R.id.tvFileAttente)
        val etRecherche = findViewById<EditText>(R.id.etRecherche)

        // Met à jour tous les compteurs et la file d'attente
        fun rafraichirStats() {
            tvCompteur.text = "Inscriptions : ${participants.size}"
            tvCapacities.text = gestion.capacitesGlobales()
            tvFileAttente.text = gestion.fileAttenteTexte()
        }

        // Lit le formulaire et retourne un Participant valide, ou null
        fun lireParticipant(): Participant? {
            val nom = etNom.text.toString().trim()
            val prenom = etPrenom.text.toString().trim()
            val age = etAge.text.toString().toIntOrNull()
            val groupe = spGroupe.selectedItem.toString()
            val support = if (rgSupport.checkedRadioButtonId == R.id.rbEmulateur) "Émulateur" else "Téléphone"

            if (nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(this, "Nom et prénom obligatoires", Toast.LENGTH_SHORT).show()
                return null
            }
            if (age == null || age !in 1..120) {
                Toast.makeText(this, "Âge invalide (1-120)", Toast.LENGTH_SHORT).show()
                return null
            }
            if (groupe == "Choisir un groupe") {
                Toast.makeText(this, "Choisissez un groupe", Toast.LENGTH_SHORT).show()
                return null
            }
            if (!cbConfirmation.isChecked) {
                Toast.makeText(this, "Confirmez les informations", Toast.LENGTH_SHORT).show()
                return null
            }
            return Participant(nom, prenom, age, groupe, support)
        }

        // Afficher la fiche (prévisualisation)
        btnAfficher.setOnClickListener {
            val p = lireParticipant() ?: return@setOnClickListener
            tvResultat.text = p.fiche()
        }

        // Enregistrer un participant
        btnEnregistrer.setOnClickListener {
            val p = lireParticipant() ?: return@setOnClickListener
            val ok = gestion.inscrire(p)
            if (ok) {
                Toast.makeText(this, "Enregistré — ${gestion.statut(p)}", Toast.LENGTH_SHORT).show()
                tvResultat.text = p.fiche()
                rafraichirStats()
            } else {
                Toast.makeText(this, "Ce participant est déjà inscrit", Toast.LENGTH_SHORT).show()
            }
        }

        // Afficher la liste complète
        btnListe.setOnClickListener {
            if (participants.isEmpty()) {
                tvResultat.text = "Aucune inscription"
            } else {
                val sb = StringBuilder()
                for (p in participants) {
                    sb.appendLine("${p.prenom} ${p.nom} (${p.groupe}) — ${gestion.statut(p)}")
                }
                tvResultat.text = sb.toString().trim()
            }
        }

        // Rechercher par nom
        btnRechercher.setOnClickListener {
            val r = etRecherche.text.toString().trim()
            if (r.isEmpty()) {
                Toast.makeText(this, "Saisissez un nom à rechercher", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var trouve: Participant? = null
            for (p in participants) {
                if (p.nom.equals(r, ignoreCase = true)) {
                    trouve = p
                    break
                }
            }
            tvResultat.text = trouve?.let { "${it.fiche()} | ${gestion.statut(it)}" }
                ?: "Aucun participant trouvé"
        }

        // Effacer le formulaire (ne supprime PAS les participants)
        btnEffacer.setOnClickListener {
            etNom.text.clear()
            etPrenom.text.clear()
            etAge.text.clear()
            spGroupe.setSelection(0)
            rgSupport.check(R.id.rbEmulateur)
            cbConfirmation.isChecked = false
            tvResultat.text = getString(R.string.resultat_initial)
        }

        // Supprimer un participant (confirmation + promotion automatique)
        btnSupprimer.setOnClickListener {
            val nom = etNom.text.toString().trim()
            val prenom = etPrenom.text.toString().trim()
            if (nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(this, "Saisissez le nom et le prénom à supprimer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cible = participants.firstOrNull {
                it.nom.equals(nom, ignoreCase = true) &&
                it.prenom.equals(prenom, ignoreCase = true)
            }
            if (cible == null) {
                Toast.makeText(this, "Participant introuvable", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Confirmer la suppression")
                .setMessage("Supprimer ${cible.prenom} ${cible.nom} (${cible.groupe}) ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    val etaitConfirme = cible.ticketAttente == null
                    gestion.supprimer(cible)
                    tvResultat.text = "Participant supprimé"
                    rafraichirStats()
                    if (etaitConfirme) {
                        Toast.makeText(this, "Un participant en attente a été promu", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        // Transférer un participant vers un autre groupe
        btnTransferer.setOnClickListener {
            val nom = etNom.text.toString().trim()
            val prenom = etPrenom.text.toString().trim()
            val cible = participants.firstOrNull {
                it.nom.equals(nom, ignoreCase = true) &&
                it.prenom.equals(prenom, ignoreCase = true)
            }
            if (cible == null) {
                Toast.makeText(this, "Renseignez le nom et le prénom du participant à transférer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val groupes = arrayOf("G1", "G2", "G3")
            val disponibles = groupes.filter { it != cible.groupe }
            AlertDialog.Builder(this)
                .setTitle("Transférer ${cible.prenom} ${cible.nom}")
                .setItems(disponibles.toTypedArray()) { _, which ->
                    val nouveau = disponibles[which]
                    val destinationLibre = gestion.placesLibres(nouveau) > 0
                    val action = {
                        gestion.changerGroupe(cible, nouveau)
                        tvResultat.text = "${cible.prenom} ${cible.nom} → $nouveau (${gestion.statut(cible)})"
                        rafraichirStats()
                    }
                    if (destinationLibre) {
                        action()
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("Groupe complet")
                            .setMessage("Ce transfert placera ${cible.prenom} en attente dans $nouveau. Continuer ?")
                            .setPositiveButton("Confirmer") { _, _ -> action() }
                            .setNegativeButton("Annuler", null)
                            .show()
                    }
                }
                .show()
        }

        // Initialisation de l'affichage
        rafraichirStats()
    }
}