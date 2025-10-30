package com.ethiopiancalendar.data.local.entity

import java.time.DayOfWeek

/**
 * Data class representing a recurrence rule for events.
 * This maps to the iCalendar RRULE format used by Google Calendar.
 *
 * Example RRULE formats:
 * - Non-repeating: null
 * - Weekly on Tuesday and Thursday: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
 * - Weekly on Monday until specific date: "RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL=20251231T235959Z"
 * - Daily for 10 occurrences: "RRULE:FREQ=DAILY;COUNT=10"
 *
 * Reference: https://datatracker.ietf.org/doc/html/rfc5545#section-3.3.10
 */
data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val weekDays: Set<DayOfWeek> = emptySet(),         // Days of week (for WEEKLY frequency)
    val endOption: RecurrenceEndOption = RecurrenceEndOption.NEVER,
    val endDate: Long? = null,                          // End date timestamp (for UNTIL end option)
    val count: Int? = null                              // Number of occurrences (for COUNT end option)
)

enum class RecurrenceFrequency(val rruleValue: String) {
    NONE(""),
    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY")
}

enum class RecurrenceEndOption {
    NEVER,      // No end date
    UNTIL,      // Ends on specific date
    COUNT       // Ends after N occurrences
}

/**
 * Extension function to convert RecurrenceRule to RRULE string format.
 *
 * Examples:
 * - Weekly on Tu, Th, never ends: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH"
 * - Weekly on Mo, until date: "RRULE:FREQ=WEEKLY;BYDAY=MO;UNTIL=20251231T235959Z"
 * - Daily, 10 times: "RRULE:FREQ=DAILY;COUNT=10"
 */
fun RecurrenceRule.toRRuleString(): String? {
    if (frequency == RecurrenceFrequency.NONE) return null

    val parts = mutableListOf<String>()
    parts.add("FREQ=${frequency.rruleValue}")

    // Add weekdays for WEEKLY frequency
    if (frequency == RecurrenceFrequency.WEEKLY && weekDays.isNotEmpty()) {
        val byDay = weekDays.sortedBy { it.value }
            .joinToString(",") { it.toRRuleDay() }
        parts.add("BYDAY=$byDay")
    }

    // Add end condition
    when (endOption) {
        RecurrenceEndOption.UNTIL -> {
            endDate?.let {
                val date = java.time.Instant.ofEpochMilli(it)
                    .atZone(java.time.ZoneId.of("UTC"))
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"))
                parts.add("UNTIL=$date")
            }
        }
        RecurrenceEndOption.COUNT -> {
            count?.let { parts.add("COUNT=$it") }
        }
        RecurrenceEndOption.NEVER -> { /* No additional part */ }
    }

    return "RRULE:${parts.joinToString(";")}"
}

/**
 * Extension function to convert DayOfWeek to RRULE day abbreviation.
 */
fun DayOfWeek.toRRuleDay(): String = when (this) {
    DayOfWeek.MONDAY -> "MO"
    DayOfWeek.TUESDAY -> "TU"
    DayOfWeek.WEDNESDAY -> "WE"
    DayOfWeek.THURSDAY -> "TH"
    DayOfWeek.FRIDAY -> "FR"
    DayOfWeek.SATURDAY -> "SA"
    DayOfWeek.SUNDAY -> "SU"
}

/**
 * Extension function to parse RRULE string into RecurrenceRule object.
 *
 * Example: "RRULE:FREQ=WEEKLY;BYDAY=TU,TH" -> RecurrenceRule(WEEKLY, [TU, TH], NEVER)
 */
fun String.parseRRule(): RecurrenceRule? {
    if (!this.startsWith("RRULE:")) return null

    val ruleParts = this.removePrefix("RRULE:").split(";")
    var frequency = RecurrenceFrequency.NONE
    val weekDays = mutableSetOf<DayOfWeek>()
    var endOption = RecurrenceEndOption.NEVER
    var endDate: Long? = null
    var count: Int? = null

    for (part in ruleParts) {
        val (key, value) = part.split("=", limit = 2)
        when (key) {
            "FREQ" -> frequency = RecurrenceFrequency.entries.find { it.rruleValue == value } ?: RecurrenceFrequency.NONE
            "BYDAY" -> {
                weekDays.addAll(value.split(",").mapNotNull { it.parseDayOfWeek() })
            }
            "UNTIL" -> {
                endOption = RecurrenceEndOption.UNTIL
                endDate = try {
                    java.time.ZonedDateTime.parse(
                        value,
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    ).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            }
            "COUNT" -> {
                endOption = RecurrenceEndOption.COUNT
                count = value.toIntOrNull()
            }
        }
    }

    return RecurrenceRule(frequency, weekDays, endOption, endDate, count)
}

/**
 * Parse RRULE day abbreviation to DayOfWeek.
 */
fun String.parseDayOfWeek(): DayOfWeek? = when (this) {
    "MO" -> DayOfWeek.MONDAY
    "TU" -> DayOfWeek.TUESDAY
    "WE" -> DayOfWeek.WEDNESDAY
    "TH" -> DayOfWeek.THURSDAY
    "FR" -> DayOfWeek.FRIDAY
    "SA" -> DayOfWeek.SATURDAY
    "SU" -> DayOfWeek.SUNDAY
    else -> null
}
