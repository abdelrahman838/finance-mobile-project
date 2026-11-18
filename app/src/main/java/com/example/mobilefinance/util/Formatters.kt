package com.example.mobilefinance.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val moneyFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

/** 3200.0 -> "+$3,200.00", -84.2 -> "-$84.20" */
fun formatSignedMoney(value: Double): String {
    val sign = if (value < 0) "-" else "+"
    return sign + "$" + moneyFormat.format(abs(value))
}

fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))

fun formatShortDate(millis: Long): String =
    SimpleDateFormat("MMM d", Locale.US).format(Date(millis))

fun formatMonthYear(millis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(millis))

/** Midnight of the day [millis] falls in. */
fun startOfDay(millis: Long): Long = Calendar.getInstance().run {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    timeInMillis
}

fun startOfMonth(millis: Long): Long = Calendar.getInstance().run {
    timeInMillis = startOfDay(millis)
    set(Calendar.DAY_OF_MONTH, 1)
    timeInMillis
}

fun startOfYear(millis: Long): Long = Calendar.getInstance().run {
    timeInMillis = startOfMonth(millis)
    set(Calendar.MONTH, Calendar.JANUARY)
    timeInMillis
}

const val DAY_MILLIS = 24 * 60 * 60 * 1000L
