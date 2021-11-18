package app.wefridge.wefridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.placeholder.PlaceholderContent
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * A fragment representing a list of Foodstuff items.
 */
class PantryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_pantry_list, container, false)

        val recycleView = mainView.findViewById<RecyclerView>(R.id.list)

        with(recycleView) {
            layoutManager =  LinearLayoutManager(context)
            adapter = MyItemRecyclerViewAdapter(PlaceholderContent.ITEMS)
        }

        mainView.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            findNavController().navigate(R.id.action_from_list_to_edit)
        }
        return mainView
    }
}