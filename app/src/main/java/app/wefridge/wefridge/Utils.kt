package app.wefridge.wefridge

import android.os.Build
import android.view.View
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.fragment_edit.*
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * Generate the md5 hash.
 * https://stackoverflow.com/a/64171625/11271734
 */
fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(trim().lowercase().toByteArray())).toString(16).padStart(32, '0')
}

// DatePicker and Date Utils


fun getDateFrom(datePicker: DatePicker): Date {
    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    return calendar.time
}

fun buildDateStringFrom(date: Date?): String {
    return if (date != null) DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(date.time)
    else ""
}

@RequiresApi(Build.VERSION_CODES.O)
fun setDatePickerDate(datePicker: DatePicker, date: Date) {
    val bestByDate = convertToLocalDate(date)
    datePicker.updateDate(bestByDate.year, bestByDate.monthValue - 1, bestByDate.dayOfMonth)

}

@RequiresApi(Build.VERSION_CODES.O)
fun convertToLocalDate(date: Date): LocalDate {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}