package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time

class AppointmentFormService(database: Database) : ServiceBase(database, AppointmentForms) {
    companion object {
        fun appointmentFormBuilder(passengerID: String?, drivingDate: String?): String {
            val passengerIDExpr = passengerID.buildConditionalExpr { "passenger_id = \"$it\"" }
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }

            return """
                select appointment_form_id,special_needs,s.station_name as pick_up_station,e.station_name as drop_off_station,driving_date,departure_time,route_number,outbound_return
                from AppointmentForms, stations as s, stations as e
                where $passengerIDExpr and $drivingDateExpr and pick_up_location_X=s.location_X and pick_up_location_Y=s.location_Y 
                    and drop_off_location_X=e.location_X and drop_off_location_Y=e.location_Y
            """
        }
    }

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

    suspend fun readAppointmentForm(passengerID: String?, drivingDate: String?): List<ExposedAppointmentForm> =
        dbQuery {
            queryAndMap(appointmentFormBuilder(passengerID, drivingDate)) {
                ExposedAppointmentForm(
                    it.getString("appointment_form_id"),
                    it.getBoolean("special_needs"),
                    it.getString("pick_up_station"),
                    it.getString("drop_off_station"),
                    it.getLocalDate("driving_date"),
                    it.getLocalTime("departure_time"),
                    it.getString("route_number"),
                    it.getEnum("outbound_return")
                )
            }
        }

    suspend fun addAppointmentForm(appointmentForm: BodyAppointmentForm) = dbQuery {
        val pickUpStation = StationService.Stations.selectAll()
            .where { StationService.Stations.stationName eq appointmentForm.pickUpStation }
            .singleOrNull() ?: return@dbQuery
        val dropOffStation = StationService.Stations.selectAll()
            .where { StationService.Stations.stationName eq appointmentForm.dropOffStation }
            .singleOrNull() ?: return@dbQuery
        val routeData = RouteService.Routes.selectAll()
            .where { RouteService.Routes.routeNumber eq appointmentForm.routeNumber }
            .singleOrNull() ?: return@dbQuery

        AppointmentForms.insert {
            it[appointmentFormId] = appointmentForm.appointmentFormId
            it[specialNeeds] = appointmentForm.specialNeeds
            it[pickUpLocationX] = pickUpStation[StationService.Stations.locationX]
            it[pickUpLocationY] = pickUpStation[StationService.Stations.locationY]
            it[dropOffLocationX] = dropOffStation[StationService.Stations.locationX]
            it[dropOffLocationY] = dropOffStation[StationService.Stations.locationY]
            it[passengerId] = appointmentForm.passengerId
            it[drivingDate] = appointmentForm.drivingDate
            it[departureTime] = appointmentForm.departureTime
            it[drivingWeek] = appointmentForm.drivingWeek
            it[routeNumber] = appointmentForm.routeNumber
            it[jurisdictionUnit] = routeData[RouteService.Routes.jurisdictionUnit]
            it[outboundReturn] = appointmentForm.outboundReturn
        }
    }

    suspend fun deleteAppointmentForm(appointmentFormId: String) = dbQuery {
        AppointmentForms.deleteWhere { AppointmentForms.appointmentFormId eq appointmentFormId }
    }
}
