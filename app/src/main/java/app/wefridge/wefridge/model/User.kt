package app.wefridge.wefridge.model

import app.wefridge.wefridge.md5

data class User(val id: String, val name: String, private val _image: String?) {
    override fun toString(): String = name
    val image: String = _image ?: "https://www.gravatar.com/avatar/${name.md5()}?s=64&d=wavatar"
}