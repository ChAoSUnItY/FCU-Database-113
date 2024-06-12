package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.update

@Serializable
data class ExposedDriver(
    val driverId: String,
    val birthDay: LocalDate,
    val gender: String,
    val violationRecord: Int,
    val driverLicense: String,
    val driverLicenseEd: String,
)

class DriverService(database: Database) : ServiceBase(database, Drivers) {
    companion object {
        fun driverBuilder(
            drivingDate: String?,
            routeNumber: String?,
            departureTime: String?,
            vehicleLicensePlate: String?
        ): String {
            val driverDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val departureTimeExpr = departureTime.buildConditionalExpr { "departure_time = \"$it\"" }
            val vehicleLicensePlateExpr = vehicleLicensePlate.buildConditionalExpr { "vehicle_license_plate = \"$it\"" }

            return """
                select *
                from Drivers
                where Driver_id in(
                    select Driver_ID
                    from ActualFrequency
                    where $driverDateExpr and $routeNumberExpr and $departureTimeExpr and $vehicleLicensePlateExpr
                )
            """
        }
    }

    object Drivers : Table() {
        val driverId = char("driver_id", 7)
        val birthday = date("birthday")
        val gender = varchar("gender", 6)
        val violationRecord = integer("violation_record")
        val driverLicense = char("driver_license", 5)
        val driverLicenseEd = date("driverLicenseEd")

        override val primaryKey = PrimaryKey(driverId)
    }

    suspend fun readDrivers(
        drivingDate: String?,
        routeNumber: String?,
        departureTime: String?,
        vehicleLicensePlate: String?
    ): List<ExposedDriver> =
        dbQuery {
            queryAndMap(driverBuilder(drivingDate, routeNumber, departureTime, vehicleLicensePlate)) {
                ExposedDriver(
                    it.getString("driver_id"),
                    it.getLocalDate("birthday"),
                    it.getString("gender"),
                    it.getInt("violation_record"),
                    it.getString("driver_license"),
                    it.getString("driverLicenseEd")
                )
            }
        }

    suspend fun addDriver(driver: BodyDriver) = dbQuery {
        Drivers.insert {
            it[driverId] = driver.driverId
            it[birthday] = driver.birthDay
            it[gender] = driver.gender
            it[violationRecord] = driver.violationRecord
            it[driverLicense] = driver.driverLicense
            it[driverLicenseEd] = driver.driverLicenseEd
        }
    }

    suspend fun updateDriver(driver: BodyDriver) = dbQuery {
        Drivers.update({ Drivers.driverId eq driver.driverId }) {
            it[birthday] = driver.birthDay
            it[gender] = driver.gender
            it[violationRecord] = driver.violationRecord
            it[driverLicense] = driver.driverLicense
            it[driverLicenseEd] = driver.driverLicenseEd
        }
    }
}