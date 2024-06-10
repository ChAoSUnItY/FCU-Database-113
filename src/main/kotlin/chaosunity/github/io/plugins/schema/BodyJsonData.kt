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

@Serializable
data class BodyBus(
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
data class BodyDriver(
    val driverId: String,
    val birthDay: LocalDate,
    val gender: String,
    val violationRecord: Int,
    val driverLicense: String,
    val driverLicenseEd: LocalDate,
)
