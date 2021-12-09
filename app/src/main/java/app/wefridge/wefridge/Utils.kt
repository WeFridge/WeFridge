package app.wefridge.wefridge

import android.app.AlertDialog
import android.content.Context
import androidx.annotation.StringRes
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Generate the md5 hash.
 * https://stackoverflow.com/a/64171625/11271734
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(trim().lowercase().toByteArray())).toString(16).padStart(32, '0')
}