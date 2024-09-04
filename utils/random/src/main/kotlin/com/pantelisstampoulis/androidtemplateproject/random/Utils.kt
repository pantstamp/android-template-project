package com.pantelisstampoulis.androidtemplateproject.random

import androidx.annotation.VisibleForTesting
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

@VisibleForTesting
val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

@VisibleForTesting
val charHexPool: List<Char> = listOf('A', 'B', 'C', 'D', 'E', 'F') + ('0'..'9')

fun randomString(
    length: Int = 10,
    characterPool: List<Char> = charPool,
): String = (1..length)
    .map { Random.nextInt(0, characterPool.size).let { characterPool[it] } }
    .joinToString("")

fun randomInt(
    from: Int = 0,
    until: Int = 9,
): Int = Random.nextInt(from = from, until = until)

fun randomLong(
    from: Long = 0,
    until: Long = 9,
): Long = Random.nextLong(from = from, until = until)

fun randomFloat(
    from: Float = 0f,
    until: Float = 9f,
): Float {
    require(until > from) {
        "$until should be greater than $from"
    }
    return Random.nextFloat() * (until - from) + from
}

fun randomBoolean(): Boolean = Random.nextBoolean()

inline fun <reified T : Enum<T>> randomEnum(): T = enumValues<T>().random()

fun randomColor(): String = "#${randomHexString()}${randomHexString()}${randomHexString()}"

private fun randomHexString(): String = randomString(
    length = 2,
    characterPool = charHexPool,
)

fun randomLocalDateTime(
    year: Int = randomInt(from = 1970, until = 2050),
    month: Month = randomEnum(),
    dayOfMonth: Int = randomInt(from = 1, until = 28),
    hour: Int = randomInt(from = 0, until = 23),
    minute: Int = randomInt(from = 0, until = 59),
    second: Int = randomInt(from = 0, until = 59),
): LocalDateTime {
    validateYear(year = year)
    validateDayOfMonth(year = year, month = month, dayOfMonth = dayOfMonth)
    validateHour(hour = hour)
    validateMinute(minute = minute)
    validateSecond(second = second)
    return LocalDateTime(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
        hour = hour,
        minute = minute,
        second = second,
    )
}

private fun validateYear(year: Int) {
    val minYear = Instant.DISTANT_PAST.toLocalDateTime(timeZone = TimeZone.UTC).year
    val maxYear = Instant.DISTANT_FUTURE.toLocalDateTime(timeZone = TimeZone.UTC).year
    require(value = year in minYear..maxYear) {
        "Wrong year: $year provided"
    }
}

private fun validateDayOfMonth(
    year: Int,
    dayOfMonth: Int,
    month: Month,
) {
    val lastDayOfMonth = LocalDate(
        year = year,
        month = month,
        dayOfMonth = 1,
    ).lastDayOfMonth.dayOfMonth
    require(value = dayOfMonth in 1..lastDayOfMonth) {
        "Wrong day: $dayOfMonth provided for month: $month and year: $year"
    }
}

private fun validateHour(hour: Int) {
    require(value = hour in 0..23) {
        "Wrong hour: $hour provided"
    }
}

private fun validateMinute(minute: Int) {
    require(value = minute in 0..59) {
        "Wrong minute: $minute provided"
    }
}

private fun validateSecond(second: Int) {
    require(value = second in 0..59) {
        "Wrong second: $second provided"
    }
}

private val LocalDate.atStartOfMonth: LocalDate
    get() = LocalDate(
        year = this.year,
        month = this.month,
        dayOfMonth = 1,
    )

private val LocalDate.lastDayOfMonth: LocalDate
    get() = plus(1, DateTimeUnit.MONTH)
        .atStartOfMonth
        .minus(1, DateTimeUnit.DAY)

fun randomLocalDate(
    year: Int = randomInt(from = 1970, until = 2050),
    month: Month = randomEnum(),
    dayOfMonth: Int = randomInt(from = 1, until = 28),
): LocalDate {
    validateYear(year = year)
    validateDayOfMonth(year = year, month = month, dayOfMonth = dayOfMonth)
    return LocalDate(
        year = year,
        month = month,
        dayOfMonth = dayOfMonth,
    )
}

fun randomLocalTime(
    hour: Int = randomInt(from = 0, until = 23),
    minute: Int = randomInt(from = 0, until = 59),
    second: Int = randomInt(from = 0, until = 59),
): LocalTime {
    validateHour(hour = hour)
    validateMinute(minute = minute)
    validateSecond(second = second)
    return LocalTime(
        hour = hour,
        minute = minute,
        second = second,
    )
}
