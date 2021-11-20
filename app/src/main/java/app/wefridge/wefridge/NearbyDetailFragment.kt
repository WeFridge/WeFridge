package app.wefridge.wefridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.wefridge.wefridge.databinding.FragmentNearbyDetailBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent


/**
 * A simple [Fragment] subclass.
 * Use the [NearbyDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NearbyDetailFragment : Fragment() {
    private var _binding: FragmentNearbyDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: PlaceholderContent.PlaceholderItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            model = it.getParcelable(ARG_MODEL)!!
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.title = model.content

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

        binding.textId.text = model.id
        binding.textName.text = model.content
        binding.textBestBy.text = model.bestByDate
        binding.textDescription.text = model.details

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
        fun newInstance(model: PlaceholderContent.PlaceholderItem) =
            NearbyDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MODEL, model)
                }
            }
    }
}