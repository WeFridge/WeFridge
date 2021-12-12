package app.wefridge.wefridge

import android.os.Build
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import android.content.Context
import android.text.format.DateFormat
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
    return if (date != null) java.text.DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(date.time)
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

fun getBestByString(best_by: Date?, ctx: Context): String {
    if (best_by == null) {
        return ""
    }
    val today = Date()
    if (today > best_by) {
        val dateFormat = DateFormat.getDateFormat(ctx)
        return ctx.getString(R.string.best_by_overdue, dateFormat.format(best_by))
    }
    val differenceInDays =
        TimeUnit.DAYS.convert(abs(best_by.time - today.time), TimeUnit.MILLISECONDS)
    if (differenceInDays == 0L) {
        return ctx.getString(R.string.best_by_today)
    }
    return if (differenceInDays > 1) {
        ctx.getString(R.string.best_by_plural, differenceInDays.toString())
    } else {
        ctx.getString(R.string.best_by_singular)
    }
}