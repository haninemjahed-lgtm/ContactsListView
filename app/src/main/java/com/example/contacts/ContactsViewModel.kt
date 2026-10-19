package com.example.contacts

import androidx.lifecycle.ViewModel

/**
 * ViewModel qui conserve la liste des contacts et la position sélectionnée en mémoire.
 * Il survit aux changements de configuration (comme la rotation de l'écran).
 */
class ContactsViewModel : ViewModel() {

    // Liste modifiable de contacts conservée dans le ViewModel
    val contacts = mutableListOf(
        "Ahmed Ben Ali",
        "Amira Chaabane",
        "Ines Jaziri",
        "Karim Bouazizi",
        "Mohamed Gharbi",
        "Nour Hammami",
        "Sarra Trabelsi",
        "Youssef Mansouri"
    )

    // Position de l'élément sélectionné dans la liste (-1 = aucun)
    var positionSelectionnee = -1

    /**
     * Trie la liste de contacts par ordre alphabétique (insensible à la casse).
     */
    fun trier() {
        contacts.sortWith(String.CASE_INSENSITIVE_ORDER)
    }

    /**
     * Vérifie si un nom existe déjà dans la liste (insensible à la casse).
     * @param indexExclu Optionnel : index à ne pas vérifier (permet à un contact de garder son propre nom lors d'une modification).
     */
    fun existeDeja(nom: String, indexExclu: Int = -1): Boolean {
        return contacts.filterIndexed { index, _ -> index != indexExclu }
            .any { it.equals(nom, ignoreCase = true) }
    }

    /**
     * Ajoute un nouveau contact, trie la liste et sélectionne le contact inséré.
     * @return true si l'ajout a réussi, false s'il s'agit d'un doublon.
     */
    fun ajouterContact(nom: String): Boolean {
        if (existeDeja(nom)) return false

        contacts.add(nom)
        trier()
        // Repositionne l'index sur le contact nouvellement ajouté dans la liste triée
        positionSelectionnee = contacts.indexOfFirst { it.equals(nom, ignoreCase = true) }
        return true
    }

    /**
     * Modifie le contact sélectionné, trie la liste et met à jour sa position.
     * @return true si la modification a réussi, false en cas de doublon.
     */
    fun modifierContact(nouveauNom: String): Boolean {
        if (positionSelectionnee == -1) return false
        if (existeDeja(nouveauNom, indexExclu = positionSelectionnee)) return false

        contacts[positionSelectionnee] = nouveauNom
        trier()
        // Recalcule l'index du contact modifié dans la liste triée
        positionSelectionnee = contacts.indexOfFirst { it.equals(nouveauNom, ignoreCase = true) }
        return true
    }

    /**
     * Supprime le contact à l'index donné et réinitialise la sélection.
     */
    fun supprimerContact(index: Int) {
        if (index in contacts.indices) {
            contacts.removeAt(index)
            positionSelectionnee = -1
        }
    }
}