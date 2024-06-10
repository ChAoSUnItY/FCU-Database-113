package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.selectAll

class VehicleMaintenanceRecordService(database: Database) : ServiceBase(database, VehicleMaintenanceRecords) {
    companion object {
        fun vmrsBuilder(drivingDate: String?, routeNumber: String?): String {
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }

            return """
                select *
                from VehicleMaintenanceRecord
                where license in (
                    select vehicle_license_plate
                    from ActualFrequency
                    where $drivingDateExpr and $routeNumberExpr
                )
            """
        }
    }

    object VehicleMaintenanceRecords : Table("VehicleMaintenanceRecord") {
        val maintenance_date = date("maintenance_date")
        val shop = varchar("shop", 40)
        val maintenance_level = integer("maintenance_level")
        val personnel = varchar("personnel", 16)
        val license = varchar("license", 8).references(BusService.Buses.license)

        override val primaryKey = PrimaryKey(maintenance_date, license, name = "PK_vehicle_maintenance_record")
    }

    suspend fun readVMRS(drivingDate: String?, routeNumber: String?): List<ExposedVMRS> = dbQuery {
        queryAndMap(vmrsBuilder(drivingDate, routeNumber)) {
            ExposedVMRS(
                it.getLocalDate("maintenance_date"),
                it.getString("shop"),
                it.getInt("maintenance_level"),
                it.getString("personnel"),
                it.getString("license")
            )
        }
    }
}
