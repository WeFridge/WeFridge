package app.wefridge.wefridge

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.databinding.FragmentPantryBinding
import app.wefridge.wefridge.model.Item
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

const val ARG_MODEL = "model"
/**
 * [RecyclerView.Adapter] that can display a [Item].
 * TODO: Replace the implementation with code for your data type.
 */
class ItemRecyclerViewAdapter(
    private val values: List<Item>,
    @IdRes private val clickAction: Int? = null,
    val fragment: Fragment
) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewHolder = ViewHolder(
            FragmentPantryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        if (clickAction != null) {
            viewHolder.itemView.setOnClickListener {
                val pos = viewHolder.absoluteAdapterPosition
                val bundle = Bundle()
                bundle.putParcelable(ARG_MODEL, values[pos])

                parent.findNavController().navigate(clickAction, bundle)
            }
        }

        return viewHolder

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.name
        holder.bestByView.text = getBestByString(item.bestByDate)//"Best by in ${item.bestByDate} days"
        holder.sharedIcon.visibility = if (item.isShared) View.VISIBLE else View.INVISIBLE
    }

    private fun getBestByString(best_by: Date?): String {
        val ctx = fragment.activity?.applicationContext!!
        val today = Date()
        if (best_by == null) {
            return ""
        }
        if (today > best_by) {
            val dateFormat = DateFormat.getDateFormat(ctx)
            return ctx.getString(R.string.best_by_overdue, dateFormat.format(best_by))
        }
        val differenceInDays = TimeUnit.DAYS.convert(abs(best_by.time - today.time), TimeUnit.MILLISECONDS)
        if (differenceInDays == 0L) {
            return ctx.getString(R.string.best_by_today)
        }
        return if (differenceInDays > 1) {
            ctx.getString(R.string.best_by_plural, differenceInDays)
        } else {
            ctx.getString(R.string.best_by_singular)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentPantryBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.content
        val bestByView: TextView = binding.bestByDate
        val sharedIcon: ImageView = binding.sharedIcon


        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}