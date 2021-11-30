package app.wefridge.wefridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.wefridge.wefridge.databinding.FragmentPantryListBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent

/**
 * A fragment representing a list of Foodstuff items.
 */
class PantryFragment : Fragment() {

    private var _binding: FragmentPantryListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPantryListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycleView = binding.list

        with(recycleView) {
            layoutManager =  LinearLayoutManager(context)
            adapter = MyItemRecyclerViewAdapter(PlaceholderContent.ITEMS, R.id.action_from_list_to_edit)
        }

        binding.fab.setOnClickListener {
           findNavController().navigate(R.id.action_from_list_to_edit)
        }
    }
}