package chaosunity.github.io.plugins

import chaosunity.github.io.plugins.schema.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:sqlite:data/data.db",
        driver = "org.sqlite.JDBC"
    )
    TransactionManager.manager.defaultIsolationLevel =
        Connection.TRANSACTION_SERIALIZABLE
    val busService = BusService(database)
    val driverService = DriverService(database)
    val passengerService = PassengerService(database)
    val stationService = StationService(database)
    val routeService = RouteService(database)
    val scheduleService = ScheduleServices(database)
    val actualFrequencyService = ActualFrequencyService(database)
    val vehicleMaintenanceRecordService = VehicleMaintenanceRecordService(database)
    val appointmentFormService = AppointmentFormService(database)
    val dockService = DockService(database)
    val historicalArrivalService = HistoricalArrivalService(database)

    routing {
        get("/bus") {
            val license = call.request.queryParameters["license"]
            val routeNumber = call.request.queryParameters["routeNumber"]

            if (license != null && routeNumber != null) {
                call.respond(HttpStatusCode.BadRequest, "Expects either query parameter license or routeNumber not null")
            } else if (license != null) {
                val bus = busService.read(license)

                if (bus != null) {
                    call.respond(HttpStatusCode.OK, bus)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } else if (routeNumber != null) {
                val buses = busService.readByRouteNumber(routeNumber)

                call.respond(HttpStatusCode.OK, buses)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Expects query parameter license or routeNumber")
            }
        }

        get("/route") {
            val stationNamePattern =
                call.request.queryParameters["stationNamePattern"]

            if (stationNamePattern != null) {
                val routes = routeService.readByStationName(stationNamePattern)

                call.respond(HttpStatusCode.OK, routes)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Expects query parameter stationNamePattern")
            }
        }

        get("/historical_arrival") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val drivingDate = call.request.queryParameters["drivingDate"]?.let(kotlinx.datetime.LocalDate::parse)

            if (routeNumber == null) {
                call.respond(HttpStatusCode.BadRequest, "Expects query parameter routeNumber not null ")
                return@get
            }

            val historicalArrivals = historicalArrivalService.readHistoricalArrivalsByRouteAndDate(routeNumber, drivingDate)

            call.respond(HttpStatusCode.OK, historicalArrivals)
        }
    }
}
