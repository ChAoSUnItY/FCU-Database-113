package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.time

class ScheduleServices(database: Database) : ServiceBase(database, Schedules) {
    object Schedules : Table("Schedules") {
        val departureTime = time("departure_time")
        val drivingWeek = char("driving_week", 3)
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn = enumerationByName("outbound_return", 1, OutboundReturnType::class)

        override val primaryKey = PrimaryKey(departureTime, drivingWeek, jurisdictionUnit, routeNumber, outboundReturn, name = "PK_schedules")

        init {
            foreignKey(
                jurisdictionUnit to RouteService.Routes.jurisdictionUnit,
                routeNumber to RouteService.Routes.routeNumber,
                outboundReturn to RouteService.Routes.outboundReturn
            )
        }
    }
}
