package app.wefridge.wefridge.model

import android.content.Context
import androidx.annotation.StringRes
import app.wefridge.wefridge.R

/*
* getByValue(value: Int?) and related code was partially inspired from:
* https://stackoverflow.com/questions/53523948/how-do-i-create-an-enum-from-an-int-in-kotlin/53524077
* */
enum class Unit(val value: Int, @StringRes val _display: Int) {
    GRAM(0, R.string.item_unit_gram),
    KILOGRAM(1, R.string.item_unit_kilogram),
    LITER(2, R.string.item_unit_liter),
    MILLILITER(3, R.string.item_unit_milliliter),
    OUNCE(4, R.string.item_unit_ounce),
    PIECE(5, R.string.item_unit_piece);
    companion object {
        private val VALUES = values()

        fun getByValue(value: Int?) = VALUES.firstOrNull { it.value == value }

        fun getByString(symbol: String, callingFragment: Fragment): Unit? {
            return when (symbol) {
                callingFragment.getString(R.string.itemUnitGramText) -> GRAM
                callingFragment.getString(R.string.itemUnitKilogramText) -> KILOGRAM
                callingFragment.getString(R.string.itemUnitLiterText) -> LITER
                callingFragment.getString(R.string.itemUnitMilliliterText) -> MILLILITER
                callingFragment.getString(R.string.itemUnitOunceText) -> OUNCE
                callingFragment.getString(R.string.itemUnitPieceText) -> PIECE
                else -> null
            }
        }
    }

    fun display(ctx: Context): String = ctx.getString(_display)
}