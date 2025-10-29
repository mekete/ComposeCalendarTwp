package com.ethiopiancalendar.ui.converter

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.extra.chrono.EthiopicDate
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import javax.inject.Inject

@HiltViewModel
class DateConverterViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DateConverterUiState())
    val uiState: StateFlow<DateConverterUiState> = _uiState.asStateFlow()

    // Gregorian to Ethiopian conversion
    fun setGregorianDate(day: String, month: String, year: String) {
        _uiState.value = _uiState.value.copy(
            gregorianDay = day,
            gregorianMonth = month,
            gregorianYear = year,
            gregorianError = null
        )
    }

    fun convertToEthiopian() {
        try {
            val day = _uiState.value.gregorianDay.toIntOrNull()
            val month = _uiState.value.gregorianMonth.toIntOrNull()
            val year = _uiState.value.gregorianYear.toIntOrNull()

            if (day == null || month == null || year == null) {
                _uiState.value = _uiState.value.copy(
                    gregorianError = "Please enter valid numbers"
                )
                return
            }

            if (day !in 1..31 || month !in 1..12 || year < 1) {
                _uiState.value = _uiState.value.copy(
                    gregorianError = "Please enter valid date values"
                )
                return
            }

            val gregorianDate = LocalDate.of(year, month, day)
            val ethiopianDate = EthiopicDate.from(gregorianDate)

            val ethiopianYear = ethiopianDate.get(ChronoField.YEAR_OF_ERA)
            val ethiopianMonth = ethiopianDate.get(ChronoField.MONTH_OF_YEAR)
            val ethiopianDay = ethiopianDate.get(ChronoField.DAY_OF_MONTH)

            val monthName = getEthiopianMonthName(ethiopianMonth)
            val result = "$monthName $ethiopianDay, $ethiopianYear"

            _uiState.value = _uiState.value.copy(
                ethiopianResult = result,
                gregorianError = null
            )

            Timber.d("Converted Gregorian to Ethiopian: $result")

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                gregorianError = "Invalid date: ${e.message}"
            )
            Timber.e(e, "Error converting Gregorian to Ethiopian")
        }
    }

    fun setGregorianDateFromPicker(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            gregorianDay = date.dayOfMonth.toString(),
            gregorianMonth = date.monthValue.toString(),
            gregorianYear = date.year.toString()
        )
    }

    // Ethiopian to Gregorian conversion
    fun setEthiopianDate(day: String, month: String, year: String) {
        _uiState.value = _uiState.value.copy(
            ethiopianDay = day,
            ethiopianMonth = month,
            ethiopianYear = year,
            ethiopianError = null
        )
    }

    fun convertToGregorian() {
        try {
            val day = _uiState.value.ethiopianDay.toIntOrNull()
            val month = _uiState.value.ethiopianMonth.toIntOrNull()
            val year = _uiState.value.ethiopianYear.toIntOrNull()

            if (day == null || month == null || year == null) {
                _uiState.value = _uiState.value.copy(
                    ethiopianError = "Please enter valid numbers"
                )
                return
            }

            // Ethiopian calendar validation
            val maxDay = when (month) {
                in 1..12 -> 30
                13 -> if (year % 4 == 3) 6 else 5 // Pagume
                else -> 0
            }

            if (month !in 1..13 || day !in 1..maxDay || year < 1) {
                _uiState.value = _uiState.value.copy(
                    ethiopianError = "Please enter valid Ethiopian date values"
                )
                return
            }

            val ethiopianDate = EthiopicDate.of(year, month, day)
            val gregorianDate = LocalDate.from(ethiopianDate)

            val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            val result = gregorianDate.format(formatter)

            _uiState.value = _uiState.value.copy(
                gregorianResult = result,
                ethiopianError = null
            )

            Timber.d("Converted Ethiopian to Gregorian: $result")

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                ethiopianError = "Invalid date: ${e.message}"
            )
            Timber.e(e, "Error converting Ethiopian to Gregorian")
        }
    }

    fun setEthiopianDateFromPicker(date: EthiopicDate) {
        _uiState.value = _uiState.value.copy(
            ethiopianDay = date.get(ChronoField.DAY_OF_MONTH).toString(),
            ethiopianMonth = date.get(ChronoField.MONTH_OF_YEAR).toString(),
            ethiopianYear = date.get(ChronoField.YEAR_OF_ERA).toString()
        )
    }

    fun clearResults() {
        _uiState.value = DateConverterUiState()
    }

    private fun getEthiopianMonthName(month: Int): String {
        return when (month) {
            1 -> "Meskerem"
            2 -> "Tikimt"
            3 -> "Hidar"
            4 -> "Tahsas"
            5 -> "Tir"
            6 -> "Yekatit"
            7 -> "Megabit"
            8 -> "Miazia"
            9 -> "Ginbot"
            10 -> "Sene"
            11 -> "Hamle"
            12 -> "Nehase"
            13 -> "Pagume"
            else -> "Unknown"
        }
    }
}

data class DateConverterUiState(
    // Gregorian inputs
    val gregorianDay: String = "",
    val gregorianMonth: String = "",
    val gregorianYear: String = "",
    val ethiopianResult: String = "",
    val gregorianError: String? = null,

    // Ethiopian inputs
    val ethiopianDay: String = "",
    val ethiopianMonth: String = "",
    val ethiopianYear: String = "",
    val gregorianResult: String = "",
    val ethiopianError: String? = null
)