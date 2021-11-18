package app.wefridge.wefridge

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.wefridge.wefridge.databinding.FragmentSettingsBinding
import app.wefridge.wefridge.databinding.FragmentSettingsParticipantAddBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

var SETTINGS_EMAIL = "SETTINGS_EMAIL"
var SETTINGS_NAME = "SETTINGS_NAME"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var sp: SharedPreferences
    private lateinit var email: String
    private lateinit var name: String
    private val participantsRecyclerViewAdapter = SettingsParticipantsRecyclerViewAdapter {
        // TODO: remove from firestore
        Log.v("Auth", "delete: ${it.name}")
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser!!

        email = sp.getString(SETTINGS_EMAIL, user.email!!)!!
        name = sp.getString(SETTINGS_NAME, user.displayName!!)!!
        binding.contactEmail.editText!!.setText(email)
        binding.contactName.editText!!.setText(name)

        // TODO: load from firebase
        participantsRecyclerViewAdapter.setItems(
            listOf(
                PlaceholderContent.ParticipantItem(
                    "1",
                    "randy.the.man@example.com",
                    "https://lh3.googleusercontent.com/a-/AOh14GhER-CTt8Fk0N3u2zvsumXQYfC3FFRcdWR4Y-v8XQ=s96-c"
                ),
                PlaceholderContent.ParticipantItem("2", "pascal@bosym.de"),
                PlaceholderContent.ParticipantItem("3", "erika1956@example.com")
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logout.setOnClickListener {
            // clear preferences on logout
            sp.edit {
                clear()
                apply()
            }

            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    (activity as MainActivity).authWall()
                }
        }

        // validate email
        val contactEmail = binding.contactEmail
        val contactEmailTextEdit = contactEmail.editText!!
        contactEmailTextEdit.addTextChangedListener {
            val content = it.toString()
            val isValid = Patterns.EMAIL_ADDRESS.matcher(content).matches()
            // TODO: error message from strings file
            contactEmail.error = if (isValid) null else "Email wrong"
        }
        contactEmailTextEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener
            if (contactEmail.error != null)
                return@setOnFocusChangeListener

            val content = contactEmailTextEdit.text.toString()
            if (content == email)
                return@setOnFocusChangeListener

            email = content
            sp.edit {
                putString(SETTINGS_EMAIL, email)
                apply()
            }
            // TODO: error message from strings file
            Toast.makeText(context, "Email saved!", Toast.LENGTH_SHORT).show()
        }

        // validate name
        val contactName = binding.contactName
        val contactNameTextEdit = contactName.editText!!
        contactNameTextEdit.addTextChangedListener {
            val content = it.toString()
            val isValid = content.isNotBlank()
            // TODO: error message from strings file
            contactName.error = if (isValid) null else "Name cannot be empty!"
        }
        contactNameTextEdit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                return@setOnFocusChangeListener
            if (contactName.error != null)
                return@setOnFocusChangeListener

            val content = contactNameTextEdit.text.toString()
            if (content == name)
                return@setOnFocusChangeListener

            name = content
            sp.edit {
                putString(SETTINGS_NAME, name)
                apply()
            }
            // TODO: error message from strings file
            Toast.makeText(context, "Name saved!", Toast.LENGTH_SHORT).show()
        }


        with(binding.participants) {
            layoutManager = LinearLayoutManager(context)
            adapter = participantsRecyclerViewAdapter
        }

        binding.inviteParticipants.setOnClickListener {
            val addBinding =
                FragmentSettingsParticipantAddBinding.inflate(layoutInflater, null, false)
            val participant = addBinding.participant
            val editText = participant.editText!!

            val dialog = MaterialAlertDialogBuilder(it.context)
                .setTitle("Add a participant")
                .setView(addBinding.root)
                .setNeutralButton("Cancel") { _, _ -> }
                .setPositiveButton("Invite") { _, _ ->
                    // TODO: check if user exists (firestore)
                    val newParticipant = editText.text.toString()
                    Log.v("Auth", newParticipant)
                    participantsRecyclerViewAdapter.addItem(
                        PlaceholderContent.ParticipantItem(
                            newParticipant,
                            newParticipant
                        )
                    )
                }.show()

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = false
            val ownEmail = FirebaseAuth.getInstance().currentUser!!.email
            editText.addTextChangedListener { it2 ->
                val content = it2.toString()
                val isValid = Patterns.EMAIL_ADDRESS.matcher(content).matches()
                // TODO: error message from strings file
                participant.error = if (isValid) null else "Email wrong"
                if (content == ownEmail)
                    participant.error = "Own email!"

                okButton.isEnabled = participant.error == null
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}