package app.wefridge.wefridge

import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat


class SwipeToDeleteCallback(private val onSwipedToDelete: (position: Int) -> Unit, ctx: Context) :
// the following lines are based on
//   - https://developer.android.com/reference/androidx/recyclerview/widget/ItemTouchHelper.SimpleCallback
//   - https://androidapps-development-blogs.medium.com/swipe-gestures-in-recyclerview-swipe-to-delete-and-archive-in-recyclerview-84bf2102c999
    ItemTouchHelper.SimpleCallback(NO_DRAG, DELETE_SWIPE_DIR) {
    private val deleteIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_baseline_delete_24)
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
    private val iconWidth = deleteIcon!!.intrinsicWidth
    private val iconHeight = deleteIcon!!.intrinsicHeight


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
    // responsible for drawing red background color and displaying the trash icon
    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, // the distance the row got swiped horizontally
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val rowView = viewHolder.itemView
        val rowHeight = rowView.bottom - rowView.top
        val swipeIsCanceled = (dX == 0f && !isCurrentlyActive)

        if (!swipeIsCanceled) {

            background.color = backgroundColor
            background.setBounds( // fill row with red color up to the point where the row is swiped to
                rowView.right + dX.toInt(),
                rowView.top,
                rowView.right,
                rowView.bottom
            )
            background.draw(canvas) // display and update the background


            // Calculate position of trash icon
            // leave equal space between the row bounds and the icon (top / bottom / right).
            val iconMargin = (rowHeight - iconHeight) / 2
            val iconTop = rowView.top + iconMargin
            val iconLeft = rowView.right - iconMargin - iconWidth
            val iconRight = rowView.right - iconMargin
            val iconBottom = iconTop + iconHeight
            val iconDisplayThreshold = iconMargin + iconWidth

            // display the trash icon on the swiped row
            deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            if (dX.toInt() < -iconDisplayThreshold) deleteIcon?.draw(canvas)
        }
    }

    companion object {
        private const val NO_DRAG = 0
        private const val DELETE_SWIPE_DIR = ItemTouchHelper.LEFT
    }
}