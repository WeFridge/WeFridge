package app.wefridge.wefridge.datamodel

import java.lang.Exception
import kotlin.Unit

interface ItemControllerInterface {

    fun getItems(callbackOnSuccess: (ArrayList<Item>) -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit)
    fun deleteItem(item: Item)
    fun saveItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit)

}