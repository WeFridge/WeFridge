package app.wefridge.wefridge

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import app.wefridge.wefridge.databinding.FragmentNearbyDetailBinding
import app.wefridge.wefridge.model.Item
import com.google.firebase.auth.FirebaseAuth


/**
 * A simple [Fragment] subclass.
 * Use the [NearbyDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NearbyDetailFragment : Fragment() {
    private var _binding: FragmentNearbyDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: Item
    private lateinit var sp: SharedPreferences
    private lateinit var email: String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            model = it.getParcelable(ARG_MODEL)!!
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.title = model.name
        loadContactInfo()

    }

    override fun onStart() {
        super.onStart()

        loadContactInfo()
    }

    private fun loadContactInfo() {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        val user = FirebaseAuth.getInstance().currentUser!!

        email = sp.getString(SETTINGS_EMAIL, user.email!!)!!
        name = sp.getString(SETTINGS_NAME, user.displayName!!)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNearbyDetailBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: populate textviews with actual datamodel
        binding.quantity.text = context?.getString(R.string.item_quantity_unit, model.quantity, model.unit.display(requireContext()))
        binding.bestBy.text = DateFormat.getDateFormat(context).format(model.bestByDate)
        binding.distance.text = ""
        binding.additionalInformation.text = model.description
        binding.owner.text = model.contactName

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            type = "text/plain"
            data = Uri.parse("mailto:")
            // TODO: get mail from model
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@arboristapp.com"))
            putExtra(Intent.EXTRA_SUBJECT, "WeFridge: ${model.name}")
            putExtra(
                Intent.EXTRA_TEXT, """Hello,

Shared item: ${model.name}

Best regards,
$name
            """.trimIndent()
            )
        }
        binding.contactButton.setOnClickListener {
            startActivity(emailIntent)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param model Datamodel.
         * @return A new instance of fragment NearbyDetailFragment.
         */
        @JvmStatic
        fun newInstance(model: Item) =
            NearbyDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MODEL, model)
                }
            }
    }
}