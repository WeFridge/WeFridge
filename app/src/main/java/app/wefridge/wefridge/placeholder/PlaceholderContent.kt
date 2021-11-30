package app.wefridge.wefridge.placeholder

import android.os.Parcelable
import android.util.Log
import app.wefridge.wefridge.MyItemRecyclerViewAdapter
import app.wefridge.wefridge.PantryFragment
import app.wefridge.wefridge.databinding.FragmentPantryListBinding
import app.wefridge.wefridge.datamodel.Item
import app.wefridge.wefridge.datamodel.ItemController
import app.wefridge.wefridge.datamodel.ItemControllerInterface
import app.wefridge.wefridge.datamodel.Unit
import app.wefridge.wefridge.md5
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * Array containing all the items displayed in the 'pantry' section.
     */
    val ITEMS: MutableList<Item> = ArrayList()


    init {
        // Add some sample items.
        val controller: ItemControllerInterface = ItemController()
        controller.getItems ({ items ->
            fillItemCollection(items)
        },
            {
                /* do nothing yet on failure */
            } )
    }

    private fun fillItemCollection(items: ArrayList<Item>) {
        for (item in items) {
            addItem(item)
        }
    }

    private fun addItem(item: Item) {
        ITEMS.add(item)
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