package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time

@Serializable
data class ExposedHistoricalArrival(
    val drivingDate: LocalDate,
    val departureTime: LocalTime,
    val drivingWeek: String,
    val jurisdiction: String,
    val routeNumber: String,
    val outboundReturn: OutboundReturnType,
    val locationX: Float,
    val locationY: Float,
    val arrivalTime: LocalTime
)

class HistoricalArrivalService(database: Database) : ServiceBase(database, HistoricalArrivals) {
    companion object {
        fun historicalArrivalSelectorBuilder(
            routeNumber: String?,
            drivingDate: String?,
            outboundReturn: OutboundReturnType?
        ): String {
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\""}

            return """
                select driving_date,station_name,arrival_time
                from HistoricalArrivals, Stations
                where HistoricalArrivals.location_x = Stations.location_X and HistoricalArrivals.location_y = Stations.location_y 
                    and $routeNumberExpr and $outboundReturnExpr and $drivingDateExpr
                order by arrival_time ASC
            """
        }
    }

    object HistoricalArrivals : Table("HistoricalArrivals") {
        val drivingDate = date("driving_date")
        val departureTime = time("departure_time")
        val drivingWeek = char("driving_week", 3)
        val jurisdictionUnit = varchar("jurisdiction_unit", 30)
        val routeNumber = char("route_number", 5)
        val outboundReturn = enumerationByName("outbound_return", 1, OutboundReturnType::class)
        val locationX = float("location_x")
        val locationY = float("location_y")
        val arrivalTime = time("arrival_time")

        override val primaryKey = PrimaryKey(
            drivingDate,
            departureTime,
            drivingWeek,
            jurisdictionUnit,
            routeNumber,
            outboundReturn,
            locationX,
            locationY,
            arrivalTime
        )

        init {
            foreignKey(
                drivingDate to ActualFrequencyService.ActualFrequencies.drivingDate,
                departureTime to ActualFrequencyService.ActualFrequencies.departureTime,
                drivingWeek to ActualFrequencyService.ActualFrequencies.drivingWeek,
                jurisdictionUnit to ActualFrequencyService.ActualFrequencies.jurisdictionUnit,
                routeNumber to ActualFrequencyService.ActualFrequencies.routeNumber,
                outboundReturn to ActualFrequencyService.ActualFrequencies.outboundReturn
            )

            foreignKey(
                locationX to StationService.Stations.locationX,
                locationY to StationService.Stations.locationY
            )
        }
    }

    suspend fun readHistoricalArrivals(
        routeNumber: String?,
        drivingDate: String?,
        outboundReturn: OutboundReturnType?
    ): List<ExposedSimpleHistoricalArrival> = dbQuery {
        queryAndMap(historicalArrivalSelectorBuilder(routeNumber, drivingDate, outboundReturn)) {
            ExposedSimpleHistoricalArrival(
                LocalDate.parse(it.getString("driving_date")),
                it.getString("station_name"),
                LocalTime.parse(it.getString("arrival_time"))
            )
        }
    }
}
