package chaosunity.github.io.plugins.schema

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll

@Serializable
data class ExposedRoute(
    val jurisdictionUnit: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType,
    val startingPointX: Float,
    val startingPointY: Float,
    val destinationX: Float,
    val destinationY: Float,
)

@Serializable
enum class OutboundReturnType {
    O,
    R
}

class RouteService(database: Database) : ServiceBase(database, Routes) {
    object Routes : Table() {
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = varchar("route_number", 5)
        val outboundReturn = enumerationByName("outbound_return", 1, OutboundReturnType::class)
        val startingPointX = float("starting_point_x")
        val startingPointY = float("starting_point_y")
        val destinationX = float("destination_x")
        val destinationY = float("destination_y")

        override val primaryKey = PrimaryKey(jurisdictionUnit, routeNumber, outboundReturn)

        init {
            foreignKey(
                startingPointX to StationService.Stations.locationX,
                startingPointY to StationService.Stations.locationY
            )

            foreignKey(
                destinationX to StationService.Stations.locationX,
                destinationY to StationService.Stations.locationY
            )
        }
    }

    suspend fun readByStationName(stationNamePattern: String): List<ExposedRoute> = dbQuery {
        StationService.Stations
            .innerJoin(
                DockService.Docks,
                { locationX },
                { locationX }) { StationService.Stations.locationY eq DockService.Docks.locationY }
            .innerJoin(Routes, { DockService.Docks.routeNumber }, { routeNumber })
            .selectAll()
            .where { StationService.Stations.stationName like stationNamePattern }
            .distinctBy { it[Routes.routeNumber] }
            .map {
                ExposedRoute(
                    it[Routes.jurisdictionUnit],
                    it[Routes.routeNumber],
                    it[Routes.outboundReturn],
                    it[Routes.startingPointX],
                    it[Routes.startingPointY],
                    it[Routes.destinationX],
                    it[Routes.destinationY],
                )
            }
    }
}
