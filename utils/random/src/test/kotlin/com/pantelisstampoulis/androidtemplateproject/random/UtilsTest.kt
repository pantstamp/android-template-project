package com.pantelisstampoulis.androidtemplateproject.random

import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun randomStringTest() {
        val text = randomString()
        assertTrue {
            text.length == 10 &&
                text.all { c -> c in charPool }
        }
        val length = 5
        val text2 = randomString(length = length)
        assertTrue {
            text2.length == length &&
                text2.all { c -> c in charPool }
        }
    }

    @Test
    fun randomIntTest() {
        val number = randomInt()
        assertTrue { number in 0..<9 }

        val from = 10
        val until = 20
        val number2 = randomInt(from = from, until = until)
        assertTrue { number2 in from..<until }
    }

    @Test
    fun randomLongTest() {
        val number = randomLong()
        assertTrue { number in 0..<9 }

        val from = 10L
        val until = 50L
        val number2 = randomLong(from = from, until = until)
        assertTrue { number2 in from..<until }
    }

    @Test
    fun randomFloatSuccessTest() {
        val number = randomFloat()
        assertTrue { number in 0f..9f }

        val from = 10f
        val until = 50f
        val number2 = randomFloat(from = from, until = until)
        assertTrue { number2 in from..until }
    }

    @Test
    fun randomFloatFailureTest() {
        assertFailsWith<IllegalArgumentException> {
            randomFloat(from = 10f, until = 8f)
        }

        assertFailsWith<IllegalArgumentException> {
            randomFloat(from = 0f, until = 0f)
        }
    }

    @Test
    fun randomBooleanTest() {
        val value = randomBoolean()
        assertTrue { value || !value }
    }

    @Test
    fun randomEnumTest() {
        val value = randomEnum<TestEnum>()
        assertTrue { value in TestEnum.entries }
    }

    private enum class TestEnum {
        Test1,
        Test2,
        Test3,
        Test4,
    }

    @Test
    fun randomColorTest() {
        val value = randomColor()
        assertTrue {
            value.length == 7 &&
                value.substring(1..<value.length).all { c ->
                    c in charHexPool
                }
        }
    }

    @Test
    fun randomLocalDateTimeSuccessTest() {
        val year = 1988
        val month = Month.MARCH
        val dayOfMonth = 9
        val hour = 12
        val minute = 30
        val second = 30
        val localDateTime = randomLocalDateTime(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hour = hour,
            minute = minute,
            second = second,
        )

        assertTrue {
            year == localDateTime.year &&
                month == localDateTime.month &&
                dayOfMonth == localDateTime.dayOfMonth &&
                hour == localDateTime.hour &&
                minute == localDateTime.minute &&
                second == localDateTime.second
        }
    }

    @Test
    fun randomLocalDateTimeFailureTest() {
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(year = Int.MIN_VALUE)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(year = Int.MAX_VALUE)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(month = Month.JANUARY, dayOfMonth = 32)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(month = Month.JANUARY, dayOfMonth = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(hour = 24)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(hour = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(minute = 60)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(minute = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(second = 60)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(second = -1)
        }
    }

    @Test
    fun randomLocalDateSuccessTest() {
        val year = 1988
        val month = Month.MARCH
        val dayOfMonth = 9
        val localDate = randomLocalDate(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
        )

        assertTrue {
            year == localDate.year &&
                month == localDate.month &&
                dayOfMonth == localDate.dayOfMonth
        }
    }

    @Test
    fun randomLocalDateFailureTest() {
        assertFailsWith<IllegalArgumentException> {
            randomLocalDate(year = Int.MIN_VALUE)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDate(month = Month.JANUARY, dayOfMonth = 32)
        }
    }

    @Test
    fun randomLocalTimeSuccessTest() {
        val localTime = randomLocalTime()
        assertTrue {
            localTime.hour in 0..23 &&
                localTime.minute in 0..59 &&
                localTime.second in 0..59
        }

        val hour = 12
        val minute = 30
        val second = 30
        val localTime2 = randomLocalTime(
            hour = hour,
            minute = minute,
            second = second,
        )

        assertTrue {
            hour == localTime2.hour &&
                minute == localTime2.minute &&
                second == localTime2.second
        }
    }

    @Test
    fun randomLocalTimeFailureTest() {
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(hour = 24)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(minute = 60)
        }
        assertFailsWith<IllegalArgumentException> {
            randomLocalDateTime(second = 60)
        }
    }
}
