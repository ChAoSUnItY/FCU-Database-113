package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.time

class ScheduleServices(database: Database) : ServiceBase(database, Schedules) {
    companion object {
        fun scheduleSelectorBuilder(
            routeNumber: String?,
            outboundReturn: OutboundReturnType?,
            drivingWeek: String?
        ): String {
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }
            val drivingWeekExpr = drivingWeek.buildConditionalExpr { "driving_week = \"$it\"" }

            return """
                select *
                from Schedules
                where $routeNumberExpr and $outboundReturnExpr and $drivingWeekExpr
            """
        }
    }

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

    suspend fun readSchedules(
        routeNumber: String?,
        outboundReturn: OutboundReturnType?,
        drivingWeek: String?
    ): List<ExposedSchedule> = dbQuery {
        queryAndMap(scheduleSelectorBuilder(routeNumber, outboundReturn, drivingWeek)) {
            ExposedSchedule(
                it.getLocalTime("departure_time"),
                it.getString("driving_week"),
                it.getString("jurisdiction_unit"),
                it.getString("route_number"),
                it.getEnum("outbound_return"),
            )
        }
    }
}
