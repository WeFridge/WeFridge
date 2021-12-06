package app.wefridge.wefridge

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.wefridge.wefridge.databinding.FragmentPantryListBinding
import app.wefridge.wefridge.datamodel.ItemController
import app.wefridge.wefridge.datamodel.ItemControllerInterface
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

        setUpRecyclerViewWithItems()

        binding.fab.setOnClickListener {
           findNavController().navigate(R.id.action_from_list_to_edit)
        }
    }

    private fun setUpRecyclerViewWithItems() {
        val recycleView = binding.list
        val itemController: ItemControllerInterface = ItemController()
        itemController.getItems({ items ->
            PlaceholderContent.items = items
            with(recycleView) {
                layoutManager = LinearLayoutManager(context)
                adapter =
                    MyItemRecyclerViewAdapter(PlaceholderContent.items, R.id.action_from_list_to_edit)
            }
        }, {
            displayAlertOnGetItemsFailed()
        })
    }

    private fun displayAlertOnGetItemsFailed() {
        AlertDialog.Builder(requireContext())
            .setTitle("Error loading your foodstuff")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("Retry") { _, _ ->
                setUpRecyclerViewWithItems()
            }
            .show()
    }
}