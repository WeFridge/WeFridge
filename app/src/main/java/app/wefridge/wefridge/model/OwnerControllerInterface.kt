package app.wefridge.wefridge.model

import com.google.firebase.firestore.DocumentReference

interface OwnerControllerInterface {
    fun getCurrentUser(callback: (DocumentReference) -> kotlin.Unit)
}