package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time

class ActualFrequencyService(database: Database) : ServiceBase(database, ActualFrequencies) {
    object ActualFrequencies : Table("ActualFrequency") {
        val drivingDate = date("driving_date")
        val departureTime = time("departure_time")
        val drivingWeek = char("driving_week", 3)
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn = enumeration("outbound_return", OutboundReturnType::class)
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

//    init {
//        val t = TransactionManager.current()
//        val fk = ForeignKeyConstraint(
//            mapOf(
//                ActualFrequencies.departureTime to ScheduleServices.Schedules.departureTime,
//                ActualFrequencies.drivingWeek to ScheduleServices.Schedules.drivingWeek,
//                ActualFrequencies.jurisdictionUnit to ScheduleServices.Schedules.jurisdictionUnit,
//                ActualFrequencies.routeNumber to ScheduleServices.Schedules.routeNumber,
//                ActualFrequencies.outboundReturn to ScheduleServices.Schedules.outboundReturn,
//            ),
//            null,
//            null,
//            null,
//        )
//
//        t.exec(fk.createStatement().first())
//
//        val fk2 = ForeignKeyConstraint(
//            ActualFrequencies.driverId,
//            DriverService.Drivers.driverId,
//            null,
//            null,
//            null
//        )
//
//        t.exec(fk2.createStatement().first())
//
//        val fk3 = ForeignKeyConstraint(
//            ActualFrequencies.vehicleLicensePlate,
//            BusService.Buses.license,
//            null,
//            null,
//            null
//        )
//
//        t.exec(fk3.createStatement().first())
//    }
}
