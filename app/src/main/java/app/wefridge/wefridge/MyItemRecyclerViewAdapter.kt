package app.wefridge.wefridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.databinding.FragmentPantryBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent.PlaceholderItem

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentPantryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.content
        holder.bestByView.text = "Best by in ${item.bestByDate} days"
        holder.sharedIcon.visibility = if (item.shared) View.VISIBLE else View.INVISIBLE
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