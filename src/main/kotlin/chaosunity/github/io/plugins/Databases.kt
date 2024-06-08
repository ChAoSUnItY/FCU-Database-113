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

            val buses = busService.readBuses(license)

            call.respond(HttpStatusCode.OK, buses)
        }

        get("/simple_bus") {
            val drivingDate = call.request.queryParameters["drivingDate"]
            val departureTime = call.request.queryParameters["departureTime"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]

            val buses = busService.readSimpleBuses(
                drivingDate,
                departureTime,
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf)
            )

            call.respond(HttpStatusCode.OK, buses)
        }

        get("/full_bus") {
            val drivingDate = call.request.queryParameters["drivingDate"]
            val departureTime = call.request.queryParameters["departureTime"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]

            val buses = actualFrequencyService.readBusByActualFrequency(
                drivingDate,
                departureTime,
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf)
            )

            call.respond(HttpStatusCode.OK, buses)
        }

        get("/route") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val startStationNamePattern = call.request.queryParameters["startStation"]
            val dockStationNamePattern = call.request.queryParameters["dockStation"]
            val endStationNamePattern = call.request.queryParameters["endStation"]

            val routes = routeService.readRoutes(
                routeNumber,
                startStationNamePattern,
                dockStationNamePattern,
                endStationNamePattern
            )

            call.respond(HttpStatusCode.OK, routes)
        }

        get("/historical_arrival") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val drivingDate = call.request.queryParameters["drivingDate"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]


            val historicalArrivals = historicalArrivalService.readHistoricalArrivals(
                routeNumber,
                drivingDate,
                outboundReturn?.let(OutboundReturnType::valueOf)
            )

            call.respond(HttpStatusCode.OK, historicalArrivals)
        }

        get("/actual_frequency") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]
            val drivingWeek = call.request.queryParameters["drivingWeek"]

            val actualFrequencies = actualFrequencyService.readActualFrequencies(
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf),
                drivingWeek
            )

            call.respond(HttpStatusCode.OK, actualFrequencies)
        }

        get("/station") {
            val stationName = call.request.queryParameters["stationName"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]

            val station = stationService.readStations(
                stationName,
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf)
            )

            call.respond(HttpStatusCode.OK, station)
        }
    }
}
