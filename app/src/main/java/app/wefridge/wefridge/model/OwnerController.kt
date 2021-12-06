package app.wefridge.wefridge.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class OwnerController: OwnerControllerInterface {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    override fun getCurrentUser(callback: (DocumentReference) -> kotlin.Unit) {
        val userID = firebaseAuth.currentUser!!.uid
        val ownerDocumentReference = firestore.collection("users").document(userID)

        return callback(ownerDocumentReference)
    }
}