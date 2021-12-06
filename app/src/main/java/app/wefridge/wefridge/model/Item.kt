package app.wefridge.wefridge.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.collections.HashMap

// The solution regarding the position of
// @RawValue was found on: https://stackoverflow.com/questions/49606163/rawvalue-annotation-is-not-applicable-to-target-value-parameter

@Parcelize
data class Item(
    var firebaseId: String? = null,
    var name: String? = null,
    var description: String? = null,
    var isShared: Boolean? = null,
    var quantity: Int? = null,
    var unit: Unit? = null,
    var bestByDate: Date? = null,
    var location: @RawValue GeoPoint? = null,
    var geohash: String? = null,
    var contactName: String? = null,
    var contactEmail: String? = null,
    var ownerReference: @RawValue DocumentReference) : Parcelable {

    init {
        if (isShared == null) isShared = false
    }

    override fun toString(): String {
        return name ?: ""
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
            "best_by" to bestByDate?.let { Timestamp(it) },
            "location" to location,
            "geohash" to geohash,
            "contact_name" to contactName,
            "contact_email" to contactEmail,
            "owner" to ownerReference
        )
    }

}