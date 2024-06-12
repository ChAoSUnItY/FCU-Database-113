package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class ExposedRoute(
    val jurisdictionUnit: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType,
    val startStation: String,
    val endStation: String,
)

@Serializable
enum class OutboundReturnType {
    O,
    R
}

@Serializable
data class ExposedActualFrequency(
    val drivingDate: LocalDate,
    val departureTime: LocalTime,
    val drivingWeek: String,
    val jurisdictionUnit: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType,
    val driverId: String,
    val vehicleLicensePlate: String,
)

@Serializable
data class ExposedStation(
    val stationName: String,
    val roadName: String,
)

@Serializable
data class ExposedSimpleBus(
    val licensePlate: String,
)

@Serializable
data class ExposedBus(
    val license: String,
    val brand: String,
    val carLength: Float,
    val lowFloor: Boolean,
    val wheelChairUse: Int,
    val numberOfSeats: Int,
    val typeAorTypeB: Boolean,
    val manualGearBox: Boolean,
    val displacement: Int,
    val maxHorsePower: Int,
    val maxTorque: Int
)

@Serializable
data class ExposedSimpleHistoricalArrival(
    val drivingDate: LocalDate,
    val stationName: String,
    val arrivalTime: LocalTime,
)

@Serializable
data class ExposedAppointmentForm(
    val appointmentFormId: String,
    val specialNeeds: Boolean,
    val pickUpStation: String,
    val dropOffStation: String,
    val drivingDate: LocalDate,
    val departureTime: LocalTime,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType
)

@Serializable
data class ExposedPassenger(
    val passengerId: String,
    val passengerName: String,
    val gender: Boolean,
    val phone: Int,
    val mail: String,
    val disabilityCategory: Int,
)

@Serializable
data class ExposedVMRS(
    val maintenanceDate: LocalDate,
    val shop: String,
    val maintenanceLevel: Int,
    val personnel: String,
    val license: String,
)

@Serializable
data class ExposedPhone(
    val phone: String
)

@Serializable
data class ExposedSchedule(
    val departureTime: LocalTime,
    val drivingWeek: String,
    val jurisdictionUnit: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType,
)
