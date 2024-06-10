package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class BodyPassenger(
    val passengerId: String,
    val passengerName: String,
    val gender: Boolean,
    val phone: Int,
    val mail: String,
    val disabilityCategory: Int,
)

@Serializable
data class BodyAppointmentForm(
    val appointmentFormId: String,
    val passengerId: String,
    val specialNeeds: Boolean,
    val pickUpStation: String,
    val dropOffStation: String,
    val drivingDate: LocalDate,
    val departureTime: LocalTime,
    val drivingWeek: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType
)
