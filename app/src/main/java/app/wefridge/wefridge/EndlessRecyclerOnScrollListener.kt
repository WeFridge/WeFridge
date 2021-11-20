package app.wefridge.wefridge

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EndlessRecyclerOnScrollListener(
    private val linearLayoutManager: LinearLayoutManager,
    private val onLoadMore: (EndlessRecyclerOnScrollListener) -> Unit
) : RecyclerView.OnScrollListener() {

    private var loading = false
    private var visibleThreshold = 2

    /**
     * https://gist.github.com/zfdang/38ae655a4fc401c99789
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy < 0)
            return

        // check for scroll down only
        val visibleItemCount = recyclerView.childCount
        val totalItemCount = linearLayoutManager.itemCount
        val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

        synchronized(this) {
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached, Do something
                loading = true
                onLoadMore.invoke(this)
            }
        }
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
    }
}