package app.wefridge.wefridge

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.databinding.FragmentSettingsParticipantBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderContent.ParticipantItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SettingsParticipantsRecyclerViewAdapter(
    private val listener: (PlaceholderContent.ParticipantItem) -> Unit
) : RecyclerView.Adapter<SettingsParticipantsRecyclerViewAdapter.ViewHolder>() {
    private var values: ArrayList<PlaceholderContent.ParticipantItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSettingsParticipantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.name.text = item.name
        // TODO: add image (either from url or gravatar)
    }

    override fun getItemCount(): Int = values.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<PlaceholderContent.ParticipantItem>) {
        values = ArrayList(list)
        notifyDataSetChanged()
    }

    fun addItem(item: PlaceholderContent.ParticipantItem) {
        values.add(item)
        notifyItemInserted(values.size - 1)
    }

    fun addItem(index: Int, item: PlaceholderContent.ParticipantItem) {
        values.add(index, item)
        notifyItemInserted(index)
    }

    fun getItem(index: Int): PlaceholderContent.ParticipantItem {
        return values[index]
    }

    inner class ViewHolder(binding: FragmentSettingsParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val avatar: ImageView = binding.avatar
        val name: TextView = binding.name
        private val delete: Button = binding.delete
        private val listenerRef: WeakReference<(PlaceholderContent.ParticipantItem) -> Unit> =
            WeakReference(listener)

        override fun toString(): String {
            return super.toString() + " '" + name.text + "'"
        }

        init {
            this.delete.setOnClickListener {
                MaterialAlertDialogBuilder(it.context)
                    .setTitle("Are you sure?")
                    .setMessage("Please confirm, that you want to remove \"${name.text}\" from your pantry.")
                    .setNeutralButton("Cancel") { _, _ -> }
                    .setPositiveButton("Confirm") { _, _ ->
                        val position = absoluteAdapterPosition
                        val toDelete = values[position]
                        listenerRef.get()?.invoke(toDelete)

                        values.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    .show()
            }
        }
    }
}