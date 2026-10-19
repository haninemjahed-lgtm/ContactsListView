package com.example.contacts

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // ViewModel persistant qui survit aux rotations de l'écran
    private val viewModel: ContactsViewModel by viewModels()

    // Adaptateur liant les données au layout item_contact.xml
    private lateinit var adapter: ArrayAdapter<String>

    // Références vers les vues du layout
    private lateinit var etContact: EditText
    private lateinit var btnAjouter: Button
    private lateinit var btnModifier: Button
    private lateinit var lvContacts: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Prise en compte des Insets système (pour ne pas passer sous la barre d'état)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Récupération des vues XML
        etContact = findViewById(R.id.etContact)
        btnAjouter = findViewById(R.id.btnAjouter)
        btnModifier = findViewById(R.id.btnModifier)
        lvContacts = findViewById(R.id.lvContacts)

        // Tri initial de la liste dans le ViewModel
        viewModel.trier()

        // Configuration de l'adaptateur relié au ViewModel
        adapter = ArrayAdapter(
            this,
            R.layout.item_contact,
            R.id.tvNom,
            viewModel.contacts
        )
        lvContacts.adapter = adapter

        // Restauration de l'élément sélectionné lors d'une récréation de l'activité (rotation)
        restaurerSelection()

        // 1) Clic simple sur un contact -> remplit l'EditText
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            viewModel.positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.setText(viewModel.contacts[position])
            etContact.setSelection(etContact.text.length)
            etContact.requestFocus()
        }

        // 2) Appui long sur un contact -> dialogue de confirmation de suppression
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            val nomContact = viewModel.contacts[position]

            AlertDialog.Builder(this)
                .setTitle(R.string.titre_supprimer)
                .setMessage(getString(R.string.msg_confirmation_supprimer, nomContact))
                .setPositiveButton(R.string.btn_oui) { _, _ ->
                    viewModel.supprimerContact(position)
                    adapter.notifyDataSetChanged()
                    lvContacts.clearChoices()
                    etContact.text.clear()
                    Toast.makeText(this, "Contact supprimé", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.btn_non, null)
                .show()

            // Retourne true pour consommer l'événement et éviter le déclenchement du clic simple
            true
        }

        // 3) Clic sur le bouton "Ajouter"
        btnAjouter.setOnClickListener { ajouterContact() }

        // 4) Clic sur le bouton "Modifier"
        btnModifier.setOnClickListener { modifierContact() }

        // 5) Action de la touche "Terminé" (Done) sur le clavier virtuel
        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (viewModel.positionSelectionnee == -1) {
                    ajouterContact()
                } else {
                    modifierContact()
                }
                true
            } else {
                false
            }
        }
    }

    private fun ajouterContact() {
        val nouveauNom = etContact.text.toString().trim()

        // Validation 1 : nom non vide
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Validation 2 : gestion des doublons via le ViewModel
        if (!viewModel.ajouterContact(nouveauNom)) {
            etContact.error = getString(R.string.msg_nom_existant)
            return
        }

        // Mise à jour de l'affichage et sélection de la nouvelle ligne
        adapter.notifyDataSetChanged()
        restaurerSelection()
        etContact.text.clear()

        Toast.makeText(this, "Contact ajouté : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    private fun modifierContact() {
        // Validation 1 : un contact doit être sélectionné
        if (viewModel.positionSelectionnee == -1) {
            Toast.makeText(this, R.string.msg_aucune_selection, Toast.LENGTH_SHORT).show()
            return
        }

        val nouveauNom = etContact.text.toString().trim()

        // Validation 2 : nom non vide
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Validation 3 : pas de doublon (sauf avec lui-même)
        if (!viewModel.modifierContact(nouveauNom)) {
            etContact.error = getString(R.string.msg_nom_existant)
            return
        }

        // Mise à jour de l'affichage et replacement du surlignage après le tri
        adapter.notifyDataSetChanged()
        restaurerSelection()

        Toast.makeText(this, "Contact modifié : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    /**
     * Maintient ou réinitialise le surlignage de l'élément sélectionné dans la ListView.
     */
    private fun restaurerSelection() {
        val pos = viewModel.positionSelectionnee
        if (pos in viewModel.contacts.indices) {
            lvContacts.setItemChecked(pos, true)
        } else {
            lvContacts.clearChoices()
            viewModel.positionSelectionnee = -1
        }
    }
}