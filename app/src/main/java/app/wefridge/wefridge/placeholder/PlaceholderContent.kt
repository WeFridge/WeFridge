package app.wefridge.wefridge.placeholder

import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.md5
import kotlin.collections.ArrayList

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * A placeholder item representing a piece of content.
     */
    data class ParticipantItem(val id: String, val name: String) {
        lateinit var image: String
        override fun toString(): String = name

        constructor(id: String, name: String, image: String?) : this(id, name) {
            if (image != null)
                this.image = image
        }

        init {
            // use image from gravatar
            val hash = name.md5()
            image = "https://www.gravatar.com/avatar/$hash?s=64&d=wavatar"
        }
    }
}