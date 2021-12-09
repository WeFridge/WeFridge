package app.wefridge.wefridge.model

import app.wefridge.wefridge.md5
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.parcel.RawValue

data class User(
    val id: String,
    val name: String,
    val email: String,
    private val _image: String?,
    val ownerReference: @RawValue DocumentReference?
) {
    override fun toString(): String = email
    val image: String = _image ?: "https://www.gravatar.com/avatar/${email.md5()}?s=64&d=wavatar"
    val ref = UserController.getUserRef(id)

    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): User {
            with(snapshot) {
                return User(
                    id,
                    getString("name") ?: "",
                    getString("email") ?: "",
                    getString("image"),
                    getDocumentReference("owner")
                )
            }
        }
    }
}