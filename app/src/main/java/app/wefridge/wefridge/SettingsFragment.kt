package app.wefridge.wefridge

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import app.wefridge.wefridge.model.User
import app.wefridge.wefridge.model.UserController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

var SETTINGS_EMAIL = "SETTINGS_EMAIL"
var SETTINGS_NAME = "SETTINGS_NAME"

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var sp: SharedPreferences
    private lateinit var email: String
    private lateinit var name: String
    private val values: ArrayList<User> = arrayListOf()
    private val participantsRecyclerViewAdapter = SettingsParticipantsRecyclerViewAdapter(values) {
        Log.v("Auth", "delete: ${it.email}")

        UserController.removeOwner(it.ref, {}, {
            // reload list
        })
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
        binding.ownerEmail.visibility = View.GONE
        binding.inviteParticipants.isEnabled = false
        binding.participants.visibility = View.GONE

        email = UserController.getLocalEmail(sp)
        name = UserController.getLocalName(sp)
        binding.contactEmail.editText!!.setText(email)
        binding.contactName.editText!!.setText(name)

        UserController.getUser({ user ->
            if (_binding == null)
                return@getUser

            if (user.ownerReference != null) {
                UserController.getUser({ owner ->
                    if (_binding != null) {
                        binding.ownerEmail.text =
                            getString(R.string.participants_manager, owner.name)
                        binding.ownerEmail.visibility = View.VISIBLE

                        // TODO: change "invite participants" button to "leave" (or add a new one)
                    }
                }, {}, user.ownerReference)
                return@getUser
            }

            UserController.getUsersParticipants({ participants ->
                if (_binding == null)
                    return@getUsersParticipants
                binding.participants.visibility = View.VISIBLE
                binding.inviteParticipants.isEnabled = true
                with(values) {
                    val oldSize = size
                    clear()
                    participantsRecyclerViewAdapter.notifyItemRangeRemoved(0, oldSize)
                }
                if (participants == null)
                    return@getUsersParticipants

                with(values) {
                    addAll(participants)
                    participantsRecyclerViewAdapter.notifyItemRangeInserted(
                        0,
                        participants.size
                    )
                    Log.v("Auth", "$size i")
                }
            }, {})

        }, { logout() })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ownerEmail.visibility = View.GONE
        binding.inviteParticipants.isEnabled = false
        binding.participants.visibility = View.GONE

        binding.logout.setOnClickListener {
            logout()
        }

        // validate email
        val contactEmail = binding.contactEmail
        val contactEmailTextEdit = contactEmail.editText!!
        contactEmailTextEdit.addTextChangedListener {
            val content = it.toString()
            val isValid = Patterns.EMAIL_ADDRESS.matcher(content).matches()
            contactEmail.error =
                if (isValid) null else getString(R.string.error_settings_contact_name_wrong)
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

            Toast.makeText(
                context,
                getString(R.string.settings_contact_email_saved),
                Toast.LENGTH_SHORT
            ).show()
        }

        // validate name
        val contactName = binding.contactName
        val contactNameTextEdit = contactName.editText!!
        contactNameTextEdit.addTextChangedListener {
            val content = it.toString()
            val isValid = content.isNotBlank()
            contactName.error =
                if (isValid) null else getString(R.string.error_settings_contact_name_empty)
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

            Toast.makeText(
                context,
                getString(R.string.settings_contact_name_saved),
                Toast.LENGTH_SHORT
            ).show()
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
                .setTitle(getString(R.string.participants_add_title))
                .setView(addBinding.root)
                .setNeutralButton(getString(R.string.participants_add_cancel)) { _, _ -> }
                .setPositiveButton(getString(R.string.participants_add_invite)) { _, _ ->
                    // TODO: check if user exists (firestore)
                    val newParticipantEmail = editText.text.toString()
                    UserController.getUserFromEmail(newParticipantEmail, { newParticipant ->
                        if (newParticipant == null) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    getString(R.string.participants_add_not_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@getUserFromEmail
                        }
                        if (newParticipant.ownerReference != null) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    getString(R.string.participants_add_already_member),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return@getUserFromEmail
                        }

                        UserController.setOwner(newParticipant.ref, {
                            with(values) {
                                add(size, newParticipant)
                                participantsRecyclerViewAdapter.notifyItemInserted(size - 1)
                            }
                            Toast.makeText(
                                context,
                                getString(R.string.participants_add_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            // TODO: maybe send a notification to this user?
                        }, {
                            Toast.makeText(
                                context,
                                getString(R.string.participants_add_failure),
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    }, {
                        Toast.makeText(
                            context,
                            getString(R.string.participants_add_failure),
                            Toast.LENGTH_SHORT
                        ).show()
                    })
                }.show()

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.isEnabled = false
            val ownEmail = FirebaseAuth.getInstance().currentUser!!.email
            editText.addTextChangedListener { it2 ->
                val content = it2.toString()
                val isValid = Patterns.EMAIL_ADDRESS.matcher(content).matches()
                participant.error =
                    if (isValid) null else getString(R.string.error_participants_email_wrong)
                if (content == ownEmail)
                    participant.error = getString(R.string.error_participants_email_own)

                okButton.isEnabled = participant.error == null
            }
        }

    }

    private fun logout() {
        UserController.unsubscribeFromMessaging(sp)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}