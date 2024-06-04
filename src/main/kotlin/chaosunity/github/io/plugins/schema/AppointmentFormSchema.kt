package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time

class AppointmentFormService(database: Database) : ServiceBase(database, AppointmentForms) {
    object AppointmentForms : Table("AppointmentForms") {
        val appointmentFormId = char("appointment_form_id", 7)
        val specialNeeds = bool("special_needs")
        val pickUpLocationX = float("pick_up_location_x")
        val pickUpLocationY = float("pick_up_location_y")
        val dropOffLocationX = float("drop_off_location_x")
        val dropOffLocationY = float("drop_off_location_y")
        val passengerId = char("passenger_id", 7)
        val drivingDate = date("driving_date")
        val departureTime = time("departure_time")
        val drivingWeek = char("driving_week", 3)
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn = enumerationByName("outbound_return", 1, OutboundReturnType::class)

        override val primaryKey = PrimaryKey(appointmentFormId)

        init {
            foreignKey(
                pickUpLocationX to StationService.Stations.locationX,
                pickUpLocationY to StationService.Stations.locationY,
            )

            foreignKey(
                dropOffLocationX to StationService.Stations.locationX,
                dropOffLocationY to StationService.Stations.locationY,
            )

            foreignKey(passengerId to PassengerService.Passengers.passengerId)

            foreignKey(
                drivingDate to ActualFrequencyService.ActualFrequencies.drivingDate,
                departureTime to ActualFrequencyService.ActualFrequencies.departureTime,
                drivingWeek to ActualFrequencyService.ActualFrequencies.drivingWeek,
                jurisdictionUnit to ActualFrequencyService.ActualFrequencies.jurisdictionUnit,
                routeNumber to ActualFrequencyService.ActualFrequencies.routeNumber,
                outboundReturn to ActualFrequencyService.ActualFrequencies.outboundReturn,
            )

            foreignKey(
                jurisdictionUnit to RouteService.Routes.jurisdictionUnit,
                routeNumber to RouteService.Routes.routeNumber,
                outboundReturn to RouteService.Routes.outboundReturn,
            )
        }
    }
}
