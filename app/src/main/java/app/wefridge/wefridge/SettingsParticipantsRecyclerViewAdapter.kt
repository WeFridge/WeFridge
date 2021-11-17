package app.wefridge.wefridge

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.databinding.FragmentSettingsParticipantBinding
import app.wefridge.wefridge.placeholder.PlaceholderContent
import java.lang.ref.WeakReference

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderContent.ParticipantItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SettingsParticipantsRecyclerViewAdapter(
    private val values: List<PlaceholderContent.ParticipantItem>,
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<SettingsParticipantsRecyclerViewAdapter.ViewHolder>() {

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
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSettingsParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val avatar: ImageView = binding.avatar
        val name: TextView = binding.name
        private val delete: Button = binding.delete
        private val listenerRef: WeakReference<(Int) -> Unit> = WeakReference(listener)

        override fun toString(): String {
            return super.toString() + " '" + name.text + "'"
        }

        init {
            this.delete.setOnClickListener {
                listenerRef.get()?.invoke(absoluteAdapterPosition)
            }
        }
    }
}