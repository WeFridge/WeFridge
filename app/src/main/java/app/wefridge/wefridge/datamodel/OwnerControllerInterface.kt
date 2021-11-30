package app.wefridge.wefridge.datamodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

interface OwnerControllerInterface {
    fun getCurrentUser(callback: (DocumentReference) -> kotlin.Unit)
}