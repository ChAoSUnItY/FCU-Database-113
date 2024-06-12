package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.*

class RouteService(database: Database) : ServiceBase(database, Routes) {
    companion object {
        fun routeSelectorBuilder(
            routeNumber: String?,
            startStationPattern: String?,
            dockStationPattern: String?,
            endStationPattern: String?
        ): String {
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val startStationExpr = startStationPattern.buildConditionalExpr { "start_station.station_name like \"$it\"" }
            val dockStationExpr = dockStationPattern.buildConditionalExpr { "station_name like \"$it\"" }
            val endStationExpr = endStationPattern.buildConditionalExpr { "end_station.station_name like \"$it\"" }

            return """
                select jurisdiction_unit, route_number, outbound_return, start_station.station_name as start_station_name, end_station.station_name as end_station_name
                from Routes, Stations as start_station, Stations as end_station
                where $routeNumberExpr
                and
                (jurisdiction_unit,route_number,outbound_return) in (select jurisdiction_unit,route_number,outbound_return 
                    from Routes, Stations
                    where starting_point_X = start_station.location_X and starting_point_y = start_station.location_Y and $startStationExpr)
                and
                (jurisdiction_unit,route_number,outbound_return) in (select jurisdiction_unit,route_number,outbound_return
                    from Routes, Stations
                    where destination_x = end_station.location_X and destination_y = end_station.location_Y and $endStationExpr)
                and exists 
                (select *
                    from Docks as dock, Stations as station
                    where dock.location_X=station.location_X and dock.location_y=station.location_y and $dockStationExpr and 
                    dock.jurisdiction_unit=Routes.jurisdiction_unit and dock.route_number=Routes.route_number and dock.outbound_return=Routes.outbound_return)
                    and start_station.location_X=starting_point_X and start_station.location_y=starting_point_y and end_station.location_X=destination_x and end_station.location_Y=destination_y
            """
        }
    }

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

    suspend fun readRoutes(
        routeNumber: String?,
        startStationPattern: String?,
        dockStationPattern: String?,
        endStationPattern: String?
    ): List<ExposedRoute> = dbQuery {
        queryAndMap(routeSelectorBuilder(routeNumber, startStationPattern, dockStationPattern, endStationPattern)) {
            ExposedRoute(
                it.getString("jurisdiction_unit"),
                it.getString("route_number"),
                OutboundReturnType.valueOf(it.getString("outbound_return")),
                it.getString("start_station_name"),
                it.getString("end_station_name")
            )
        }
    }
}
