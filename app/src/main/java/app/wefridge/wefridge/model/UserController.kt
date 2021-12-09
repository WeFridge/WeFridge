package app.wefridge.wefridge.model

import android.content.SharedPreferences
import app.wefridge.wefridge.SETTINGS_EMAIL
import app.wefridge.wefridge.SETTINGS_NAME
import app.wefridge.wefridge.USERS_COLLECTION_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserController {
    companion object {
        private val db by lazy { FirebaseFirestore.getInstance() }
        private val usersRef = db.collection(USERS_COLLECTION_NAME)
        private val firebaseAuth = FirebaseAuth.getInstance()

        fun getLocalEmail(sp: SharedPreferences): String {
            return sp.getString(SETTINGS_EMAIL, getCurrentUser()!!.email!!)!!
        }

        fun getLocalName(sp: SharedPreferences): String {
            return sp.getString(SETTINGS_NAME, getCurrentUser()!!.displayName!!)!!
        }

        fun getCurrentUserRef(): DocumentReference {
            val userID = getCurrentUser()!!.uid
            return getUserRef(userID)
        }

        fun getCurrentUser() = firebaseAuth.currentUser

        fun getUserRef(userId: String): DocumentReference {
            return usersRef.document(userId)
        }

        fun getUserFromEmail(
            email: String,
            onSuccess: (User?) -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit
        ) {
            usersRef.whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { p ->
                    if (p.isEmpty) {
                        onSuccess(null)
                        return@addOnSuccessListener
                    }
                    onSuccess(User.fromSnapshot(p.first()))
                }
                .addOnFailureListener(onFailure)
        }

        fun setOwner(
            user: DocumentReference,
            onSuccess: () -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit,
            owner: DocumentReference? = null
        ) {
            val ownerField = hashMapOf<String, Any>(
                "owner" to (owner ?: getCurrentUserRef())
            )

            user.update(ownerField)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        }

        fun removeOwner(
            user: DocumentReference,
            onSuccess: () -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit
        ) {
            val ownerField = hashMapOf<String, Any>(
                "owner" to FieldValue.delete()
            )

            user.update(ownerField)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        }


        fun getUser(
            onSuccess: (User) -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit,
            user: DocumentReference? = null
        ) {
            (user ?: getCurrentUserRef()).get()
                .addOnSuccessListener {
                    onSuccess(User.fromSnapshot(it))
                }
                .addOnFailureListener {
                    onFailure(it)
                }
        }

        fun getUsersParticipants(
            onSuccess: (List<User>?) -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit,
            userRef: DocumentReference? = null
        ) {
            usersRef
                .whereEqualTo("owner", userRef ?: getCurrentUserRef())
                .get()
                .addOnSuccessListener {
                    if (it.isEmpty) {
                        onSuccess(null)
                        return@addOnSuccessListener
                    }
                    onSuccess(it.documents.map { p ->
                        User.fromSnapshot(p)
                    })
                }
                .addOnFailureListener {
                    onFailure(it)
                }
        }
    }
}