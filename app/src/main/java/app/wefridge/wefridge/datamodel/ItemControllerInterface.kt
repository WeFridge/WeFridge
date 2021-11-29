package app.wefridge.wefridge.datamodel

interface ItemControllerInterface {

    fun getItems(): ArrayList<Item>
    fun deleteItem(item: Item)
    fun saveItem(item: Item)

}