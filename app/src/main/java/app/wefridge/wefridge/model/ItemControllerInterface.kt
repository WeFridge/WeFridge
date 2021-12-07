package app.wefridge.wefridge.model

import java.lang.Exception

interface ItemControllerInterface {

    fun getItems(callbackOnSuccess: (MutableList<Item>) -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit)
    fun deleteItem(item: Item)
    fun saveItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit)

}