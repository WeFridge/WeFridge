package app.wefridge.wefridge

import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat


class ItemSwipeToDeleteCallback(private val onSwipedToDelete: (position: Int) -> Unit, val context: Context) :
// the following lines are based on
//   - https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper.SimpleCallback
//   - https://androidapps-development-blogs.medium.com/swipe-gestures-in-recyclerview-swipe-to-delete-and-archive-in-recyclerview-84bf2102c999
    ItemTouchHelper.SimpleCallback(NO_DRAG, DELETE_SWIPE_DIR) {
    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_24)
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val inWidth = deleteIcon!!.intrinsicWidth
    private val inHeight = deleteIcon!!.intrinsicHeight
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // do nothing
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.layoutPosition
        if (direction == DELETE_SWIPE_DIR) {
            onSwipedToDelete(position)
        }
    }

    // this function was taken from https://stackoverflow.com/questions/58239860/how-to-add-layout-with-red-background-color-when-swiping-to-delete-in-recyclervi
    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (!isCanceled) {

            background.color = backgroundColor
            background.setBounds( // fill row with red color up to the point where the row is swiped to
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(canvas) // display and update the background


            // Calculate position of trash icon
            val iconTop = itemView.top + (itemHeight - inHeight) / 2
            val iconMargin = (itemHeight - inHeight) / 2
            val iconLeft = itemView.right - iconMargin - inWidth
            val iconRight = itemView.right - iconMargin
            val iconBottom = iconTop + inHeight

            // display the trash icon on the swiped row
            deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon?.draw(canvas)
        }
    }

    companion object {
        private const val NO_DRAG = 0
        private const val DELETE_SWIPE_DIR = ItemTouchHelper.LEFT
    }
}