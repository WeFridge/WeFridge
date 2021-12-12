package app.wefridge.wefridge.model

import app.wefridge.wefridge.USERS_COLLECTION_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class OwnerController {

    companion object {
        private val usersRef = FirebaseFirestore.getInstance().collection(USERS_COLLECTION_NAME)
        private val firebaseAuth = FirebaseAuth.getInstance()

        fun getCurrentUserReference(): DocumentReference? {
            val userID = firebaseAuth.currentUser?.uid
            return if (userID != null) usersRef.document(userID)
            else null
        }

        fun getCurrentUser() : FirebaseUser? {
            return firebaseAuth.currentUser
        }
    }
}