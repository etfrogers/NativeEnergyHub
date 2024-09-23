package com.example.energyhub.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class PlanningViewModel(
    private val timezone: TimeZone,
    private val nDays: Int = 7
): ViewModel() {
    private val _uiState = MutableStateFlow(
        PlanningUiState(
            timezone=timezone,
        ))
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    private var planningDays: Map<LocalDate, PlanningDay> = mapOf()

    fun daysToShow(): List<LocalDate> {
        val today = Clock.System.todayIn(timezone)
        return (0..<nDays).map {
            today.plus(DatePeriod(days = it))
        }
    }

    fun setWorkBoolean(newState: Boolean, day: LocalDate) {
        planningDays[day]!!.isGoingToWork = newState
        updateDays()
    }

    private fun updateDays() {
        _uiState.update { currentState ->
            currentState.copy(days = planningDays.values.sortedBy { it.date }.map { it.state() })
        }
    }
}

data class PlanningDay(
    val date: LocalDate,
    var isGoingToWork: Boolean = false,
    var sunshineFraction: Float = 0f,
){
    internal fun state(): DayState {
        return DayState(date, isGoingToWork, sunshineFraction)
    }
}

data class DayState(
    val date: LocalDate,
    val isGoingToWork: Boolean = false,
    val sunshineFraction: Float = 0f,
)

data class PlanningUiState(
    val timezone: TimeZone,
    val days: List<DayState> = listOf()
)