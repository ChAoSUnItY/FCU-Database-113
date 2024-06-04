package chaosunity.github.io.plugins.schema

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

@Serializable
data class ExposedBus(
    val license: String,
    val brand: String,
    val carLength: Float,
    val lowFloor: Boolean,
    val wheelChairUse: Int,
    val numberOfSeats: Int,
    val typeAorTypeB: Boolean,
    val manualGearBox: Boolean,
    val displacement: Int,
    val maxHorsePower: Int,
    val maxTorque: Int
)

class BusService(database: Database) : ServiceBase(database, Buses) {
    object Buses : Table() {
        val license = varchar("license", 8)
        val brand = varchar("brand", 24)
        val carLength = float("car_length")
        val lowFloor = bool("low_floor")
        val wheelChairUse = integer("wheel_chair_use")
        val numberOfSeats = integer("number_of_seats")
        val typeAorTypeB = bool("type_a_or_type_b")
        val manualGearBox = bool("manual_gear_box")
        val displacement = integer("displacement")
        val maxHorsePower = integer("max_horse_power")
        val maxTorque = integer("max_torque")

        override val primaryKey = PrimaryKey(license)
    }

    suspend fun add(bus: ExposedBus): String = dbQuery {
        Buses.insert {
            it[license] = bus.license
            it[brand] = bus.brand
            it[carLength] = bus.carLength
            it[lowFloor] = bus.lowFloor
            it[wheelChairUse] = bus.wheelChairUse
            it[numberOfSeats] = bus.numberOfSeats
            it[typeAorTypeB] = bus.typeAorTypeB
            it[manualGearBox] = bus.manualGearBox
            it[displacement] = bus.displacement
            it[maxHorsePower] = bus.maxHorsePower
            it[maxTorque] = bus.maxTorque
        }[Buses.license]
    }

    suspend fun read(license: String): ExposedBus? = dbQuery {
        Buses.selectAll()
            .where { Buses.license eq license }
            .map {
                ExposedBus(
                    it[Buses.license],
                    it[Buses.brand],
                    it[Buses.carLength],
                    it[Buses.lowFloor],
                    it[Buses.wheelChairUse],
                    it[Buses.numberOfSeats],
                    it[Buses.typeAorTypeB],
                    it[Buses.manualGearBox],
                    it[Buses.displacement],
                    it[Buses.maxHorsePower],
                    it[Buses.maxTorque]
                )
            }
            .singleOrNull()
    }

    suspend fun readByRouteNumber(routeNumber: String): List<ExposedBus> = dbQuery {
        ActualFrequencyService.ActualFrequencies.selectAll()
            .where { ActualFrequencyService.ActualFrequencies.routeNumber eq routeNumber }
            .distinctBy { it[ActualFrequencyService.ActualFrequencies.vehicleLicensePlate] }
            .mapNotNull {
                val vehicleLicensePlate = it[ActualFrequencyService.ActualFrequencies.vehicleLicensePlate]

                read(vehicleLicensePlate)
            }
    }

    suspend fun remove(license: String) {
        dbQuery {
            Buses.deleteWhere { Buses.license.eq(license) }
        }
    }
}
