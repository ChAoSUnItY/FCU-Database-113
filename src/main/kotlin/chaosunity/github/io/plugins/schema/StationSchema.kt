package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

class StationService(database: Database) : ServiceBase(database, Stations) {
    companion object {
        fun stationSelectorBuilder(
            stationName: String?,
            routeNumber: String?,
            outboundReturn: OutboundReturnType?
        ): String {
            val stationNameExpr = stationName.buildConditionalExpr { "station_name like \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }

            return """
                select station_name, road_name
                from stations
                where $stationNameExpr and (exists
                (select *
                    from Routes
                    where $routeNumberExpr and $outboundReturnExpr and((starting_point_X=location_X and starting_point_y=location_Y) or (destination_x=location_X and destination_y=location_Y) )
                )
                or (location_x, location_y)in
                (select location_X, location_Y
                    from Docks
                    where $routeNumberExpr and $outboundReturnExpr
                ))
            """
        }

        fun stationByDisInfoSelectorBuilder(
            stationName: String?,
            routeNumber: String?,
            outboundReturn: OutboundReturnType?,
            accessibility: String?,
            waitingAreaSeat: String?
        ): String {
            val stationNameExpr = stationName.buildConditionalExpr { "station_name like \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }
            val accessibilityExpr = accessibility.buildConditionalExpr { "accessibility = \"$it\"" }
            val waitingAreaSeatExpr = waitingAreaSeat.buildConditionalExpr { "waiting_area_seat = \"$it\"" }

            return """
                select *
                from stations 
                where $stationNameExpr and $accessibilityExpr and $waitingAreaSeatExpr and (exists
                (select *
                    from Routes
                    where $routeNumberExpr and $outboundReturnExpr and((starting_point_X=location_X and starting_point_y=location_Y) or (destination_x=location_X and destination_y=location_Y) )
                )
                or (location_x, location_y)in
                (select location_X, location_Y
                    from Docks
                    where $routeNumberExpr and $outboundReturnExpr
                ))
            """
        }
    }

    object Stations : Table() {
        val locationX = float("location_x")
        val locationY = float("location_y")
        val stationName = varchar("station_name", 50)
        val roadName = varchar("road_name", 50)
        val accessibility = bool("accessibility")
        val waitingAreaSeat = bool("waiting_area_seat")

        override val primaryKey = PrimaryKey(locationX, locationY)
    }

    suspend fun readStations(
        stationName: String?,
        routeNumber: String?,
        outboundReturn: OutboundReturnType?
    ): List<ExposedStation> = dbQuery {
        queryAndMap(stationSelectorBuilder(stationName, routeNumber, outboundReturn)) {
            ExposedStation(
                it.getString("station_name"),
                it.getString("road_name")
            )
        }
    }

    suspend fun readStationsByDisabilityInfo(
        stationName: String?,
        routeNumber: String?,
        outboundReturn: OutboundReturnType?,
        accessibility: String?,
        waitingAreaSeat: String?
    ): List<ExposedStation> = dbQuery {
        queryAndMap(stationByDisInfoSelectorBuilder(stationName, routeNumber, outboundReturn, accessibility, waitingAreaSeat)) {
            ExposedStation(
                it.getString("station_name"),
                it.getString("road_name")
            )
        }
    }
}
