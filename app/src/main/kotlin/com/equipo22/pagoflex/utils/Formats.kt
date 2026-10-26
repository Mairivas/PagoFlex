package com.equipo22.pagoflex.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val DATE_PATTERN = "dd-MM-yyyy"
const val DATE_TIME_PATTERN = "dd-MM-yyyy HH:mm"

private val locale = Locale("es", "CL")

fun formatCurrency(value: Long): String {
    val format = NumberFormat.getNumberInstance(locale)
    format.maximumFractionDigits = 0
    return "$ ${format.format(value)}"
}

fun formatRate(value: Double): String = String.format(locale, "%.1f%%", value)

fun parseDate(value: String): Date? = try {
    SimpleDateFormat(DATE_PATTERN, locale).apply { isLenient = false }.parse(value)
} catch (exception: Exception) {
    null
}

fun parseDateTime(value: String): Date? = try {
    SimpleDateFormat(DATE_TIME_PATTERN, locale).apply { isLenient = false }.parse(value)
} catch (exception: Exception) {
    null
}

fun formatDate(date: Date): String = SimpleDateFormat(DATE_PATTERN, locale).format(date)

fun formatDateTime(date: Date): String = SimpleDateFormat(DATE_TIME_PATTERN, locale).format(date)

fun nowDate(): String = formatDate(Date())

fun nowDateTime(): String = formatDateTime(Date())

fun startOfDay(date: Date): Date = Calendar.getInstance().apply {
    time = date
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.time

fun addDays(date: Date, days: Int): Date = Calendar.getInstance().apply {
    time = date
    add(Calendar.DAY_OF_YEAR, days)
}.time

fun addMonths(date: Date, months: Int): Date = Calendar.getInstance().apply {
    time = date
    add(Calendar.MONTH, months)
}.time

fun daysBetween(from: Date, to: Date): Long {
    val millis = startOfDay(to).time - startOfDay(from).time
    return millis / 86400000L
}

fun daysLate(dueDate: String): Int {
    val due = parseDate(dueDate) ?: return 0
    val difference = daysBetween(due, Date())
    return if (difference > 0) difference.toInt() else 0
}

fun daysUntil(dueDate: String): Int {
    val due = parseDate(dueDate) ?: return 0
    return -daysBetween(Date(), due).toInt()
}

fun isDueToday(dueDate: String): Boolean = dueDate == nowDate()

fun isPast(dueDate: String): Boolean {
    val due = parseDate(dueDate) ?: return false
    return startOfDay(due).before(startOfDay(Date()))
}

fun dateKey(date: Date): String {
    val calendar = Calendar.getInstance().apply { time = date }
    val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    return "$month-${calendar.get(Calendar.YEAR)}"
}

fun monthKeyOf(dateText: String): String = if (dateText.length >= 10) dateText.substring(3, 10) else dateText

fun currentMonthKey(): String = dateKey(Date())

fun monthLabel(key: String): String {
    val parts = key.split("-")
    if (parts.size != 2) return key
    val month = parts[0].toIntOrNull() ?: return key
    val names = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val name = names.getOrNull(month - 1) ?: return key
    return "$name ${parts[1]}"
}

fun lastMonthKeys(quantity: Int): List<String> {
    val result = mutableListOf<String>()
    var cursor = Date()
    repeat(quantity) {
        result.add(dateKey(cursor))
        cursor = addMonths(cursor, -1)
    }
    return result
}

fun businessDaysSince(dateText: String): Int {
    val start = parseDateTime(dateText) ?: parseDate(dateText) ?: return 0
    val cursor = Calendar.getInstance().apply { time = startOfDay(start) }
    val end = Calendar.getInstance().apply { time = startOfDay(Date()) }
    var count = 0
    while (cursor.before(end)) {
        cursor.add(Calendar.DAY_OF_YEAR, 1)
        val day = cursor.get(Calendar.DAY_OF_WEEK)
        if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) count++
    }
    return count
}

fun isOnTime(paymentDate: String, dueDate: String): Boolean {
    val paid = parseDateTime(paymentDate) ?: parseDate(paymentDate) ?: return false
    val due = parseDate(dueDate) ?: return false
    return !startOfDay(paid).after(startOfDay(due))
}

fun maskRut(rut: String): String {
    val clean = rut.replace(".", "").trim()
    val parts = clean.split("-")
    if (parts.size != 2) return rut
    val body = parts[0]
    val verifier = parts[1]
    return if (body.length >= 4) "${body.take(2)}.*.*-$verifier" else "***-$verifier"
}

fun normalizeRut(rut: String): String = rut.replace(".", "").replace(" ", "").uppercase()

fun isValidRut(rut: String): Boolean {
    val clean = normalizeRut(rut)
    if (!clean.contains("-")) return false
    val parts = clean.split("-")
    if (parts.size != 2) return false
    val body = parts[0]
    val verifier = parts[1]
    if (body.length !in 7..8 || !body.all { it.isDigit() } || verifier.length != 1) return false
    var sum = 0
    var multiplier = 2
    for (index in body.length - 1 downTo 0) {
        sum += body[index].digitToInt() * multiplier
        multiplier = if (multiplier == 7) 2 else multiplier + 1
    }
    val remainder = 11 - (sum % 11)
    val expected = when (remainder) {
        11 -> "0"
        10 -> "K"
        else -> remainder.toString()
    }
    return expected == verifier.uppercase()
}

fun cleanAmount(value: String): String = value.filter { it.isDigit() }.take(9)
