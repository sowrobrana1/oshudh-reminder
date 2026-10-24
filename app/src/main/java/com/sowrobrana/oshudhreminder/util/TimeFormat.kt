package com.sowrobrana.oshudhreminder.util

/** "সকাল ৮:০০" style Bangla clock label for minutes since midnight. */
fun formatTimeBn(minutes: Int): String {
    val hour24 = minutes / 60
    val min = minutes % 60
    val period = when (hour24) {
        in 5..11 -> "সকাল"
        12 -> "দুপুর"
        in 13..15 -> "দুপুর"
        in 16..17 -> "বিকাল"
        in 18..22 -> "রাত"
        else -> "রাত"
    }
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val minText = if (min < 10) "0$min" else "$min"
    return "$period $hour12:$minText"
}

/** ASCII digits -> Bangla digits, for counts like "৩টি". */
fun bnDigits(value: Int): String {
    val map = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯',
    )
    return value.toString().map { map[it] ?: it }.joinToString("")
}
