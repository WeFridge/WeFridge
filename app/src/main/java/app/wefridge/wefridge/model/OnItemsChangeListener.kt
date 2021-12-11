package app.wefridge.wefridge.model

import com.google.firebase.firestore.DocumentChange

interface OnItemsChangeListener {

    fun onItemChanged(type: DocumentChange.Type, atIndex: Int)
}