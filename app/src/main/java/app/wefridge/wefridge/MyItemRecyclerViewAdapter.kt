package app.wefridge.wefridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.databinding.FragmentPantryBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent.PlaceholderItem

const val ARG_MODEL = "model"
/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val values: List<PlaceholderItem>,
    @IdRes private val clickAction: Int? = null
) : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {


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
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentPantryBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}
