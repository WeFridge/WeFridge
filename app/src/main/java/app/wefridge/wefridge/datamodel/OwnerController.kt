package app.wefridge.wefridge.datamodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.Unit

class OwnerController: OwnerControllerInterface {
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()

    override fun getCurrentUser(callback: (DocumentReference) -> kotlin.Unit) {
        val userID = firebaseAuth.currentUser!!.uid
        val ownerDocumentReference = firestore.collection("users").document(userID)

        return callback(ownerDocumentReference)
    }
}