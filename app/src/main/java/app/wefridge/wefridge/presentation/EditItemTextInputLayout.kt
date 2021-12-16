package app.wefridge.wefridge.presentation

import android.R
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import com.google.android.material.textfield.TextInputLayout

class EditItemTextInputLayout : TextInputLayout {


    init {
        endIconMode = END_ICON_CLEAR_TEXT
        this.setEndIconOnClickListener {
            editText?.setText("")
        }
    }

    constructor(context: Context?) : super(ContextThemeWrapper(context, DEF_STYLE_ATTR))
    constructor(context: Context?, attrs: AttributeSet?) : super(
        ContextThemeWrapper(
            context,
            DEF_STYLE_ATTR
        ), attrs
    )
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ContextThemeWrapper(context, DEF_STYLE_ATTR),
        attrs,
        defStyleAttr
    )

    companion object {
        private val DEF_STYLE_ATTR = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            R.style.Theme_DeviceDefault_DayNight
        } else {
            R.style.Theme_DeviceDefault
        }
    }
}