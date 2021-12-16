package app.wefridge.wefridge.presentation

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.wefridge.wefridge.R
import app.wefridge.wefridge.application.model.User
import app.wefridge.wefridge.databinding.FragmentSettingsParticipantBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import java.lang.ref.WeakReference

/**
 * [RecyclerView.Adapter] that can display a [User].
 */
class SettingsParticipantsRecyclerViewAdapter(
    private val values: ArrayList<User>,
    private val listener: (User) -> Unit
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
        Log.v("Auth", "bind $position")
        val item = values[position]
        holder.name.text = item.email
        Picasso.get().load(item.image).placeholder(R.drawable.fui_ic_anonymous_white_24dp)
            .into(holder.avatar)
        holder.delete.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSettingsParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val avatar: ImageView = binding.avatar
        val name: TextView = binding.name
        val delete: Button = binding.delete
        private val listenerRef: WeakReference<(User) -> Unit> =
            WeakReference(listener)

        override fun toString(): String {
            return super.toString() + " '" + name.text + "'"
        }

        init {
            this.delete.setOnClickListener {
                val ctx = it.context
                MaterialAlertDialogBuilder(ctx)
                    .setTitle(ctx.getString(R.string.participants_remove_title))
                    .setMessage(ctx.getString(R.string.participants_remove_message, name.text))
                    .setNeutralButton(ctx.getString(R.string.participants_remove_cancel)) { _, _ -> }
                    .setPositiveButton(ctx.getString(R.string.participants_remove_confirm)) { _, _ ->
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