package chaosunity.github.io.plugins.schema

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

@Serializable
data class ExposedStation(
    val locationX: Float,
    val locationY: Float,
    val stationName: String,
    val roadName: String,
    val accessibility: Boolean,
    val waitingAreaSeat: Boolean
)

class StationService(database: Database) : ServiceBase(database, Stations) {
    object Stations : Table() {
        val locationX = float("location_x")
        val locationY = float("location_y")
        val stationName = varchar("station_name", 50)
        val roadName = varchar("road_name", 50)
        val accessibility = bool("accessibility")
        val waitingAreaSeat = bool("waiting_area_seat")

        override val primaryKey = PrimaryKey(locationX, locationY)
    }
}
