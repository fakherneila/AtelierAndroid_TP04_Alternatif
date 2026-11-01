package tn.tp.atelierandroid

// Classe de base (open autorise l'héritage)
open class Personne(val nom: String, val prenom: String, var age: Int)

// Classe fille qui hérite de Personne
class Participant(
    nom: String,
    prenom: String,
    age: Int,
    var groupe: String,
    val support: String,
    var ticketAttente: Int? = null   // null = confirmé, non-null = ticket d'attente
) : Personne(nom, prenom, age) {

    fun fiche(): String {
        val categorie = when (age) {
            in 1..17 -> "Mineur"
            in 18..25 -> "Jeune adulte"
            else -> "Adulte"
        }
        return "Nom : $nom | Prénom : $prenom | Âge : $age ans | Catégorie : $categorie | Groupe : $groupe | Support : $support"
    }
}