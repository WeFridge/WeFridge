package app.wefridge.wefridge

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.wefridge.wefridge.databinding.FragmentNerbyItemListBinding
import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.ItemController
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

/**
 * A fragment representing a list of Items.
 */
class NearbyItemFragment : Fragment() {

    private var _binding: FragmentNerbyItemListBinding? = null
    private val binding get() = _binding!!

    private val itemsPerPage = 15
    private val values = ArrayList<Item>()
    private val _adapter = ItemRecyclerViewAdapter(values, R.id.action_from_nearby_to_detail)
    private lateinit var scrollListener: EndlessRecyclerOnScrollListener
    private lateinit var refreshLayout: SwipeRefreshLayout

    private val db = Firebase.firestore
    private val itemDb = db.collection(ITEMS_COLLECTION_NAME)
    private var lastVisible: DocumentSnapshot? = null

    private var loading = false
        set(value) {
            field = value
            refreshLayout.isRefreshing = field
            scrollListener.loading = field
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNerbyItemListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.list) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = _adapter
            scrollListener = EndlessRecyclerOnScrollListener(linearLayoutManager) {
                loadPage()
            }
            addOnScrollListener(scrollListener)
        }

        refreshLayout = binding.swipe
        with(refreshLayout) {
            setOnRefreshListener {
                lastVisible = null
                loadPage()
            }
            loadPage()
        }
    }

    private fun loadPage() {
        if (loading)
            return
        loading = true
        Log.v("Auth", "Page: ${lastVisible?.id ?: "new"}")

        val oldAmount = values.size
        val query = itemDb.whereEqualTo("is_shared", true)
            .orderBy("best_by", Query.Direction.ASCENDING)
            .limit(itemsPerPage.toLong())

        if (lastVisible == null) {
            query.get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        loading = false
                        return@addOnSuccessListener
                    }
                    values.clear()

                    val newValues = it.documents.mapNotNull { item -> ItemController.tryParse(item) }
                    values.addAll(newValues)

                    val newSize = newValues.size

                    if (oldAmount > newSize) {
                        val diff = oldAmount - newSize
                        _adapter.notifyItemRangeRemoved(oldAmount - diff, diff)
                    }

                    _adapter.notifyItemRangeChanged(0, newSize)
                    lastVisible = it.documents[it.size() - 1]

                    loading = false
                }
                .addOnFailureListener {
                    Log.e("Auth", "error", it)
                }
        } else {
            query.startAfter(lastVisible!!.getDate("best_by"))
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        loading = false
                        return@addOnSuccessListener
                    }

                    val newValues = it.documents.mapNotNull { item -> ItemController.tryParse(item) }

                    if (values.addAll(newValues)) {
                        _adapter.notifyItemRangeInserted(oldAmount, newValues.size)
                    }
                    lastVisible = it.documents[it.size() - 1]

                    loading = false
                }
                .addOnFailureListener {
                    Log.e("Auth", "error", it)
                }

        }
    }
}