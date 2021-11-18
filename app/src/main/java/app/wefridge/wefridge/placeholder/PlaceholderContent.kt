package app.wefridge.wefridge.placeholder

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
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun getRandomFoodstuff(): String {
        var foodStuffList = arrayOf("Gouda cheese", "octopus",
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
        return foodStuffList[ThreadLocalRandom.current().nextInt(0, foodStuffList.size)]
    }

    private fun createPlaceholderItem(position: Int): PlaceholderItem {
        return PlaceholderItem(position.toString(),
                getRandomFoodstuff(), //TODO: Capitalize this?
                ThreadLocalRandom.current().nextInt(1,42).toString(),
                makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: String, val content: String, val bestByDate: String, val details: String) {
        override fun toString(): String = content
    }
}