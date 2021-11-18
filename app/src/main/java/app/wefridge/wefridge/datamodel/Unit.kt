package app.wefridge.wefridge.datamodel

/*
* getByValue(value: Int?) and related code was partially inspired from:
* https://stackoverflow.com/questions/53523948/how-do-i-create-an-enum-from-an-int-in-kotlin/53524077
* */
enum class Unit(val value: Int) {
    GRAM(0), KILOGRAM(1), LITER(2), MILLILITER(3), OUNCE(4), PIECE(5);

    companion object {
        private val VALUES = values()

        fun getByValue(value: Int?) = VALUES.firstOrNull { it.value == value }

    }
}