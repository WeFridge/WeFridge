package app.wefridge.wefridge.placeholder

import android.os.Parcelable
import app.wefridge.wefridge.md5
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    private val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private const val COUNT = 50

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    private fun getRandomFoodstuff(): String {
        val foodStuffList = arrayOf("Gouda cheese", "octopus",
                "granola",
                "chambord",
                "bok choy",
                "pinto beans",
                "vegemite",
                "onions",
                "clams",
                "pomegranates",
                "Canadian bacon",
                "hamburger",
                "aioli",
                "butter",
                "milk",
                "anchovy paste",
                "celery seeds",
                "coconut milk",
                "sausages",
                "Romano cheese")
        val selectedFoodStuff = foodStuffList[ThreadLocalRandom.current().nextInt(0, foodStuffList.size)]
        return selectedFoodStuff.substring(0, 1).uppercase(Locale.getDefault()) + selectedFoodStuff.substring(1)
    }

    private fun createPlaceholderItem(position: Int): PlaceholderItem {
        return PlaceholderItem(position.toString(),
                getRandomFoodstuff(),
                ThreadLocalRandom.current().nextInt(1,42).toString(),
                makeDetails(position),
                ThreadLocalRandom.current().nextBoolean())
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0 until position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A placeholder item representing a piece of content.
     */
    // TODO: adapt PlaceholderItem to Item Object later (from branch datamodel_item)
    @Parcelize
    data class PlaceholderItem(
        val id: String,
        val content: String,
        val bestByDate: String,
        val details: String,
        val shared: Boolean
    ) : Parcelable {
        override fun toString(): String = content
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class ParticipantItem(val id: String, val name: String) {
        lateinit var image: String
        override fun toString(): String = name

        constructor(id: String, name: String, image: String) : this(id, name) {
            this.image = image
        }

        init {
            // use image from gravatar
            val hash = name.md5()
            image = "https://www.gravatar.com/avatar/$hash?s=64&d=wavatar"
        }
    }
}