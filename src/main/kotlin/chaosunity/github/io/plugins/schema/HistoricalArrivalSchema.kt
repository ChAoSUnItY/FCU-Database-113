package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.time
import org.jetbrains.exposed.sql.selectAll

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
    object HistoricalArrivals : Table() {
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

    suspend fun readHistoricalArrivalsByRouteAndDate(
        routeNumber: String,
        drivingDate: LocalDate?
    ): List<ExposedHistoricalArrival> = dbQuery {
        val query = HistoricalArrivals.selectAll()

        if (drivingDate != null) {
            query.where { (HistoricalArrivals.routeNumber eq routeNumber) and (HistoricalArrivals.drivingDate eq drivingDate) }
        } else {
            query.where { (HistoricalArrivals.routeNumber eq routeNumber) }
        }

        query.mapNotNull {
            ExposedHistoricalArrival(
                it[HistoricalArrivals.drivingDate],
                it[HistoricalArrivals.departureTime],
                it[HistoricalArrivals.drivingWeek],
                it[HistoricalArrivals.jurisdictionUnit],
                it[HistoricalArrivals.routeNumber],
                it[HistoricalArrivals.outboundReturn],
                it[HistoricalArrivals.locationX],
                it[HistoricalArrivals.locationY],
                it[HistoricalArrivals.arrivalTime]
            )
        }
    }
}
