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
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return_type = \"$it\"" }

            return """
                select station_name, road_name
                from stations
                where exists
                    (select *
                    from routes
                    where $routeNumberExpr and $outboundReturnExpr and((starting_point_X=location_X and starting_point_y=location_Y) or (destination_x=location_X and destination_y=location_Y) )
                    )
                or (location_x,location_y)in
                    (	select location_X,location_Y
                        from docks
                        where $routeNumberExpr and $outboundReturnExpr
                    )
                and $stationNameExpr
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
}
