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
import app.wefridge.wefridge.model.*
import com.firebase.geofire.GeoLocation

/**
 * A fragment representing a list of Items.
 */
class NearbyItemFragment : Fragment() {

    private var _binding: FragmentNerbyItemListBinding? = null
    private val binding get() = _binding!!

    private val values = ArrayList<Item>()
    private val _adapter = ItemRecyclerViewAdapter(values, R.id.action_from_nearby_to_detail)
    private lateinit var scrollListener: EndlessRecyclerOnScrollListener
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var locationController: LocationController
    private var geoLocation: GeoLocation? = null

    private var radius: Double = 0.0

    private var loading = false
        set(value) {
            field = value
            refreshLayout.isRefreshing = field
            scrollListener.loading = field
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationController = LocationController(this,
            callbackOnPermissionDenied = { alertDialogOnLocationPermissionDenied(requireContext()).show() },
            callbackForPermissionRationale = { callback ->
                alertDialogForLocationPermissionRationale(requireContext()).setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    callback(true)
                    locationController.getCurrentLocation()
                }.show()
            },
            callbackOnDeterminationFailed = { alertDialogOnUnableToDetermineLocation(requireContext()).show() },
            callbackOnSuccess = { geoPoint ->
                geoLocation = GeoLocation(geoPoint.latitude, geoPoint.longitude)
                loading = false
                loadPage()
            })
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
                radius = 0.0
                loadPage()
            }
            loadPage()
        }
    }

    private fun loadPage() {
        if (loading)
            return
        loading = true
        if (geoLocation == null)
            return locationController.getCurrentLocation()

//        Log.v("Auth", "Page: ${lastVisible?.id ?: "new"}")
        ItemController.getNearbyItems({ items ->
            if (items.isEmpty()) {
                loading = false
                return@getNearbyItems
            }
            val oldAmount = values.size

            values.clear()
            values.addAll(items)

            val newSize = items.size

            if (oldAmount > newSize) {
                val diff = oldAmount - newSize
                _adapter.notifyItemRangeRemoved(oldAmount - diff, diff)
            }

            _adapter.notifyItemRangeChanged(0, newSize)

            radius = items.last().distance + 500.0

            loading = false
        }, {
            Log.e("Auth", "nearbyItems", it)
            loading = false
        }, radius, geoLocation!!)
    }
}