package com.ethiopiancalendar.domain.model

import androidx.compose.ui.graphics.Color

enum class HolidayType {
    NATIONAL,
    ORTHODOX_CHRISTIAN,
    MUSLIM,
    CULTURAL;
    
    fun getColor(): Color {
        return when (this) {
            NATIONAL -> Color(0xFF1976D2)      // Blue
            ORTHODOX_CHRISTIAN -> Color(0xFFF57C00)  // Orange
            MUSLIM -> Color(0xFF388E3C)         // Green
            CULTURAL -> Color(0xFF7B1FA2)       // Purple
        }
    }
}
