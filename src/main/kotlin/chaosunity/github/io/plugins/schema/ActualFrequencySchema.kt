package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time

class ActualFrequencyService(database: Database) : ServiceBase(database, ActualFrequencies) {
    companion object {
        fun busByActualFrequencySelectorBuilder(
            drivingDate: String?,
            departureTime: String?,
            routeNumber: String?,
            outboundReturn: OutboundReturnType?,
        ): String {
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val departureTimeExpr = departureTime.buildConditionalExpr { "departure_time = \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }

            return """
                select *
                from Buses
                where license in 
                    (select vehicle_license_plate
                    from ActualFrequency
                    where $drivingDateExpr and $departureTimeExpr and $routeNumberExpr and $outboundReturnExpr
                    )
            """
        }

        fun actualFrequencyByDisInfoSelectorBuilder(
            drivingDate: String?,
            routeNumber: String?,
            lowFloor: String?,
            wheelchairUse: String?
        ): String {
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val lowFloorExpr = lowFloor.buildConditionalExpr { "low_floor = \"$it\"" }
            val wheelchairUseExpr = wheelchairUse.buildConditionalExpr { "wheelchair_use = \"$it\"" }

            return """
                select *
                from ActualFrequency, Buses
                where $drivingDateExpr and $routeNumberExpr and $lowFloorExpr 
                    and $wheelchairUseExpr and vehicle_license_plate = license
            """
        }
    }

    object ActualFrequencies : Table("ActualFrequency") {
        val drivingDate = date("driving_date")
        val departureTime = time("departure_time")
        val drivingWeek = char("driving_week", 3)
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn = enumerationByName("outbound_return", 1, OutboundReturnType::class)
        val driverId = char("driver_id", 10)
        val vehicleLicensePlate = varchar("vehicle_license_plate", 8)

        override val primaryKey = PrimaryKey(
            drivingDate,
            departureTime,
            drivingWeek,
            jurisdictionUnit,
            routeNumber,
            outboundReturn,
        )

        init {
            foreignKey(
                departureTime to ScheduleServices.Schedules.departureTime,
                drivingWeek to ScheduleServices.Schedules.drivingWeek,
                jurisdictionUnit to ScheduleServices.Schedules.jurisdictionUnit,
                routeNumber to ScheduleServices.Schedules.routeNumber,
                outboundReturn to ScheduleServices.Schedules.outboundReturn,
            )

            foreignKey(
                driverId to DriverService.Drivers.driverId
            )

            foreignKey(
                vehicleLicensePlate to BusService.Buses.license
            )
        }
    }

    suspend fun readBusByActualFrequency(
        drivingDate: String?,
        departureTime: String?,
        routeNumber: String?,
        outboundReturn: OutboundReturnType?,
    ): List<ExposedBus> = dbQuery {
        queryAndMap(busByActualFrequencySelectorBuilder(drivingDate, departureTime, routeNumber, outboundReturn)) {
            ExposedBus(
                it.getString("license"),
                it.getString("brand"),
                it.getFloat("car_length"),
                it.getBoolean("low_floor"),
                it.getInt("wheel_chair_use"),
                it.getInt("number_of_seats"),
                it.getBoolean("type_a_or_type_b"),
                it.getBoolean("manual_gear_box"),
                it.getInt("displacement"),
                it.getInt("max_horse_power"),
                it.getInt("max_torque")
            )
        }
    }

    suspend fun readActualFrequenciesByDisabilityInfo(
        drivingDate: String?,
        routeNumber: String?,
        lowFloor: String?,
        wheelchairUse: String?
    ): List<ExposedActualFrequency> = dbQuery {
        queryAndMap(actualFrequencyByDisInfoSelectorBuilder(drivingDate, routeNumber, lowFloor, wheelchairUse)) {
            ExposedActualFrequency(
                it.getLocalDate("driving_date"),
                it.getLocalTime("departure_time"),
                it.getString("driving_week"),
                it.getString("jurisdiction_unit"),
                it.getString("route_number"),
                it.getEnum("outbound_return"),
                it.getString("driver_id"),
                it.getString("vehicle_license_plate"),
            )
        }
    }
}
