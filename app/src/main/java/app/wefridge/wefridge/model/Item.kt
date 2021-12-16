package app.wefridge.wefridge.model

import android.os.Parcelable
import app.wefridge.wefridge.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

// The solution regarding the position of
// @RawValue was found on: https://stackoverflow.com/questions/49606163/rawvalue-annotation-is-not-applicable-to-target-value-parameter

@Parcelize
data class Item(
    var firebaseId: String? = null,
    var name: String = "",
    var description: String? = null,
    var isShared: Boolean = false,
    var quantity: Long = 0,
    var unit: Unit = Unit.PIECE,
    var bestByDate: Date? = null,
    var location: @RawValue GeoPoint? = null,
    var geohash: String? = null,
    var contactName: String? = null,
    var contactEmail: String? = null,
    var ownerReference: @RawValue DocumentReference,
    var distance: Double = 0.0
) : Parcelable {

    override fun toString(): String {
        return name
    }

    /*
    * This code is inspired by a code snippet found on
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    fun getHashMap(): HashMap<String, Any?> {

        return hashMapOf (
            ITEM_NAME to name,
            ITEM_DESCRIPTION to description,
            ITEM_IS_SHARED to isShared,
            ITEM_QUANTITY to quantity,
            ITEM_UNIT to unit.value,
            ITEM_BEST_BY to bestByDate?.let { Timestamp(it) },
            ITEM_LOCATION to location,
            ITEM_GEOHASH to geohash,
            ITEM_CONTACT_NAME to contactName,
            ITEM_CONTACT_EMAIL to contactEmail,
            ITEM_OWNER to ownerReference
        )
    }

}