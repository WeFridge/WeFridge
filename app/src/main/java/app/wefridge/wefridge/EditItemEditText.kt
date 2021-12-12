package app.wefridge.wefridge

// this file was inspired by https://stackoverflow.com/questions/29744313/detect-dismiss-keyboard-event-in-android

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat.getSystemService


class EditItemEditText : AppCompatEditText {

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs)

    constructor(context: Context?) : super(context!!)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            // system back button pressed
            dispatchKeyEvent(event)
            dismissKeyboard()
            clearFocus()
            return true
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onEditorAction(actionCode: Int) {
        // enter pressed
        dismissKeyboard()
        clearFocus()
        super.onEditorAction(actionCode)
    }

    private fun dismissKeyboard() {
        // this code is based on https://stackoverflow.com/questions/3553779/android-dismiss-keyboard/3553966
        val imm: InputMethodManager? = getSystemService(context, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }
}