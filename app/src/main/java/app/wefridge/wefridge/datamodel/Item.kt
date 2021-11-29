package app.wefridge.wefridge.datamodel

import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.HashMap

data class Item(val firebaseId: String? = null,
                var name: String? = null,
                var description: String? = null,
                var isShared: Boolean? = null,
                var quantity: Int? = null,
                var unit: Unit? = null,
                var sharedEmail: String? = null,
                var bestByDate: Date? = null,
                var location: GeoPoint? = null,
                var geohash: String? = null,
                var contactName: String? = null,
                var contactEmail: String? = null,
                var ownerReference: DocumentReference,) {

    init {
        if (isShared == null) isShared = false
        if (quantity == null) quantity = 0
    }

    /*
    * This code is inspired by a code snippet found on
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    fun getHashMap(): HashMap<String, Any?> {

        return hashMapOf (
            "name" to name,
            "description" to description,
            "is_shared" to isShared,
            "quantity" to quantity,
            "unit" to unit?.value,
            "shared_email" to sharedEmail,
            "best_by" to bestByDate?.let { Timestamp(it) },
            "location" to location,
            "geohash" to geohash,
            "contact_name" to contactName,
            "contact_email" to contactEmail,
            "owner" to ownerReference
        )
    }

}