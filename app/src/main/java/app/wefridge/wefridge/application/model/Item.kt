package app.wefridge.wefridge.application.model

import android.os.Parcel
import android.os.Parcelable
import app.wefridge.wefridge.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.RawValue
import java.util.*

// The solution regarding the position of
// @RawValue was found on: https://stackoverflow.com/questions/49606163/rawvalue-annotation-is-not-applicable-to-target-value-parameter

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

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel) = Item(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Item>(size)
        }
    }

    private constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt() != 0,
        parcel.readLong(),
        Unit.getByValue(parcel.readInt()) ?: Unit.PIECE,
        parcel.readValue(Date::class.java.classLoader) as? Date?,
        if (parcel.readInt() != 0) GeoPoint(parcel.readDouble(), parcel.readDouble()) else null,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        UserController.getUserRef(parcel.readString()!!),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeString(firebaseId)
            writeString(name)
            writeString(description)
            writeInt(if (isShared) 1 else 0)
            writeLong(quantity)
            writeInt(unit.value)
            if (location == null) {
                writeInt(0)
            } else {
                writeInt(1)
                writeDouble(location!!.latitude)
                writeDouble(location!!.longitude)
            }
            writeString(geohash)
            writeString(contactName)
            writeString(contactEmail)
            writeString(ownerReference.id)
            writeDouble(distance)
        }
    }

    override fun describeContents() = 0
}