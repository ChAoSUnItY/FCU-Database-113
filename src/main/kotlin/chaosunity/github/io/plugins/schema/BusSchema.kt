package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class BusService(database: Database) : ServiceBase(database, Buses) {
    companion object {
        fun simpleBusSelectorBuilder(
            drivingDate: String?,
            departureTime: String?,
            routeNumber: String?,
            outboundReturn: OutboundReturnType?
        ): String {
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val departureTimeExpr = departureTime.buildConditionalExpr { "departure_time = \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }

            return """
                select vehicle_license_plate
                from ActualFrequency
                where $drivingDateExpr and $departureTimeExpr 
                    and $routeNumberExpr and $outboundReturnExpr 
            """
        }

        fun busSelectorBuilder(license: String?): String {
            val licenseExpr = license.buildConditionalExpr { "license = \"$it\"" }

            return """
                select *
                from Buses
                where $licenseExpr
            """
        }

        fun unusedBusSelectorBuilder(drivingDate: String?): String {
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }

            return """
                SELECT license
                FROM bus 
                WHERE NOT EXISTS( 
                    SELECT *
                    FROM actual_frequency 
                        WHERE $drivingDateExpr AND vehicle_license_plate = license)
            """
        }
    }

    object Buses : Table("Buses") {
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

    suspend fun readSimpleBuses(
        drivingDate: String?,
        departureTime: String?,
        routeNumber: String?,
        outboundReturn: OutboundReturnType?
    ): List<ExposedSimpleBus> = dbQuery {
        queryAndMap(simpleBusSelectorBuilder(drivingDate, departureTime, routeNumber, outboundReturn)) {
            ExposedSimpleBus(
                it.getString("vehicle_license_plate")
            )
        }
    }

    suspend fun readBuses(
        license: String?,
    ): List<ExposedBus> = dbQuery {
        queryAndMap(busSelectorBuilder(license)) {
            ExposedBus(
                it.getString("license"),
                it.getString("brand"),
                it.getFloat("car_length"),
                it.getBoolean("low_floor"),
                it.getInt("wheel_chair_use"),
                it.getInt("number_of_seats"),
                it.getBoolean("type_a_or_type_b"),
                it.getBoolean("manual_gear_box"),
                it.getInt("displacement"),
                it.getInt("max_horse_power"),
                it.getInt("max_torque")
            )
        }
    }

    suspend fun readUnusedBuses(drivingDate: String?): List<ExposedSimpleBus> = dbQuery {
        queryAndMap(unusedBusSelectorBuilder(drivingDate)) {
            ExposedSimpleBus(
                it.getString("vehicle_license_plate")
            )
        }
    }

    suspend fun addBus(bus: BodyBus) = dbQuery {
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
        }
    }

    suspend fun updateBus(bus: BodyBus) = dbQuery {
        Buses.update({ Buses.license eq bus.license }) {
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
        }
    }
}
