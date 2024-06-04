package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

class VehicleMaintenanceRecordService(database: Database) : ServiceBase(database, VehicleMaintenanceRecords) {
    object VehicleMaintenanceRecords : Table("VehicleMaintenanceRecord") {
        val maintenance_date = date("maintenance_date")
        val shop = varchar("shop", 40)
        val maintenance_level = integer("maintenance_level")
        val personnel = varchar("personnel", 16)
        val license = varchar("license", 8).references(BusService.Buses.license)

        override val primaryKey = PrimaryKey(maintenance_date, license, name = "PK_vehicle_maintenance_record")
    }
}
