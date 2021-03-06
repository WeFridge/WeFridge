package app.wefridge.wefridge.presentation

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import app.wefridge.wefridge.R
import app.wefridge.wefridge.application.model.Item
import app.wefridge.wefridge.application.model.ItemController
import app.wefridge.wefridge.application.model.UserController
import app.wefridge.wefridge.databinding.FragmentPantryListBinding
import app.wefridge.wefridge.displayToastOnInternetUnavailable
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration


/**
 * A fragment representing a list of Foodstuff items.
 */
class PantryFragment : Fragment() {

    private var _binding: FragmentPantryListBinding? = null
    private val binding get() = _binding!!
    private val values = ArrayList<Item>()
    private var snapshotListener: ListenerRegistration? = null
    private var ownerRef: DocumentReference? = null
    private val recyclerViewAdapter = ItemRecyclerViewAdapter(values, R.id.action_from_list_to_edit)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPantryListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.list

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = recyclerViewAdapter
        }

        binding.fab.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(ARG_OWNER, ownerRef?.id)

            findNavController().navigate(R.id.action_from_list_to_edit, bundle)
        }

        val itemSwipeTouchHelper =
            ItemTouchHelper(SwipeToDeleteCallback(onSwipedToDelete = { position ->
                val deletedItem = values[position]
                ItemController.deleteItem(deletedItem, {
                    if (context != null)
                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                }, { })
            }, requireContext()))

        itemSwipeTouchHelper.attachToRecyclerView(recyclerView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            displayToastOnInternetUnavailable(requireContext())
    }


    override fun onStart() {
        super.onStart()

        UserController.getCurrentUser() ?: return

        loadItems()
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    private fun loadItems() {
        snapshotListener?.remove()
        with(values) {
            val oldSize = size
            clear()
            recyclerViewAdapter.notifyItemRangeRemoved(0, oldSize)
        }

        ItemController.getItemsSnapshot({ it, ownerRef ->
            snapshotListener = it
            this.ownerRef = ownerRef
        }) { item, type, oldIndex, newIndex ->
            when (type) {
                DocumentChange.Type.ADDED -> {
                    values.add(newIndex, item!!)
                    recyclerViewAdapter.notifyItemInserted(newIndex)
                }
                DocumentChange.Type.MODIFIED -> {
                    values[newIndex] = item!!
                    recyclerViewAdapter.notifyItemChanged(newIndex)
                }
                DocumentChange.Type.REMOVED -> {
                    values.removeAt(oldIndex)
                    recyclerViewAdapter.notifyItemRemoved(oldIndex)
                }
            }
        }
    }
}