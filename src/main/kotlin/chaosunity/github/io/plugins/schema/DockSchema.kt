package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

class DockService(database: Database) : ServiceBase(database, Docks) {
    object Docks : Table("Docks") {
        val locationX = float("location_x")
        val locationY = float("location_y")
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn =
            enumerationByName("outbound_return", 1, OutboundReturnType::class)

        override val primaryKey =
            PrimaryKey(locationX, locationY, jurisdictionUnit, routeNumber, outboundReturn)

        init {
            foreignKey(
                locationX to StationService.Stations.locationX,
                locationY to StationService.Stations.locationY
            )

            foreignKey(
                jurisdictionUnit to RouteService.Routes.jurisdictionUnit,
                routeNumber to RouteService.Routes.routeNumber,
                outboundReturn to RouteService.Routes.outboundReturn
            )
        }
    }
}
