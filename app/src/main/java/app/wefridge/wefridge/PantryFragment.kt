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
import app.wefridge.wefridge.model.ItemController
import app.wefridge.wefridge.model.OnItemsChangeListener
import com.google.firebase.firestore.DocumentChange

/**
 * A fragment representing a list of Foodstuff items.
 */
class PantryFragment : Fragment(), OnItemsChangeListener {

    private var _binding: FragmentPantryListBinding? = null
    private val binding get() = _binding!!
    private var getItemsSuccessfullyCalled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPantryListBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState?.getBoolean("getItemsSuccessfullyCalled") == true) {
            setUpRecyclerViewWithItems()
        } else {
            val itemController = ItemController()
            itemController.getItems({
                    setUpRecyclerViewWithItems()
                    getItemsSuccessfullyCalled = true
                },
                {
                    displayAlertOnGetItemsFailed()
                    getItemsSuccessfullyCalled = false
                })
        }

        ItemController.addOnItemChangedListener(this)


        binding.fab.setOnClickListener {
           findNavController().navigate(R.id.action_from_list_to_edit)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("getItemsSuccessfullyCalled", getItemsSuccessfullyCalled)
    }


    override fun onDestroy() {
        super.onDestroy()
        ItemController.deleteOnItemChangedListener(this)
    }

    private fun setUpRecyclerViewWithItems() {
        val recyclerView = binding.list
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =
                ItemRecyclerViewAdapter(ItemController.items, R.id.action_from_list_to_edit, this)

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

    override fun onItemChanged(type: DocumentChange.Type, atIndex: Int) {
        val recyclerView = binding.list

        when(type) {
            DocumentChange.Type.ADDED -> recyclerView.adapter?.notifyItemInserted(atIndex)
            DocumentChange.Type.MODIFIED -> recyclerView.adapter?.notifyItemChanged(atIndex)
            DocumentChange.Type.REMOVED -> recyclerView.adapter?.notifyItemRemoved(atIndex)
        }
    }
}