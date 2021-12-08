package app.wefridge.wefridge.model

import app.wefridge.wefridge.USERS_COLLECTION_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class OwnerController {
    private val usersRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION_NAME)
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(callback: (DocumentReference) -> kotlin.Unit) {
        val userID = firebaseAuth.currentUser!!.uid
        val ownerDocumentReference = usersRef.document(userID)

        return callback(ownerDocumentReference)
    }
}