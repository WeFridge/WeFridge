package app.wefridge.wefridge

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            ItemController.getItems({
                getItemsSuccessfullyCalled = true
                setUpRecyclerViewWithItems()
            },
                {
                    getItemsSuccessfullyCalled = false
                    displayAlertOnGetItemsFailed {
                        // on retry button pressed, try to set up the list again
                        onViewCreated(view, savedInstanceState)
                    }
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
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter =
                MyItemRecyclerViewAdapter(ItemController.items, R.id.action_from_list_to_edit)
        }
    }


    private fun displayAlertOnGetItemsFailed(retryPressed: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error loading your foodstuff")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton(R.string.ad_btn_retry) { _, _ ->
                retryPressed()
            }
            .show()
    }

    override fun onItemChanged(type: DocumentChange.Type, atIndex: Int) {
        val recyclerView = binding.list

        when(type) {
            DocumentChange.Type.ADDED -> {
                recyclerView.adapter?.notifyItemInserted(atIndex)
            }
            DocumentChange.Type.MODIFIED -> {
                recyclerView.adapter?.notifyItemChanged(atIndex)
                Toast.makeText(requireContext(), "An item has been updated!", Toast.LENGTH_SHORT).show()
            }
            DocumentChange.Type.REMOVED -> {
                recyclerView.adapter?.notifyItemRemoved(atIndex)
                Toast.makeText(requireContext(), "An item has been removed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}