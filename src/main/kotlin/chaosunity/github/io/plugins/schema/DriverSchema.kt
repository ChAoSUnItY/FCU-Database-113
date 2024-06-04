package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

@Serializable
data class ExposedDriver(
    val driverId: String,
    val birthDay: LocalDate,
    val gender: String,
    val violationRecord: String,
    val driverLicense: String,
    val driverLicenseEd: String,
)

class DriverService(database: Database) : ServiceBase(database, Drivers) {
    object Drivers : Table() {
        val driverId = char("driver_id", 7)
        val birthday = date("birthday")
        val gender = varchar("gender", 6)
        val violationRecord = integer("violation_record")
        val driverLicense = char("driver_license", 5)
        val driverLicenseEd = date("driverLicenseEd")

        override val primaryKey = PrimaryKey(driverId)
    }
}