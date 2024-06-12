package chaosunity.github.io.plugins

import chaosunity.github.io.plugins.schema.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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

        get("/schedule") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]
            val drivingWeek = call.request.queryParameters["drivingWeek"]

            val schedules = scheduleService.readSchedules(
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf),
                drivingWeek
            )

            call.respond(HttpStatusCode.OK, schedules)
        }

        get("/actual_frequency_disability") {
            val drivingDate = call.request.queryParameters["drivingDate"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val lowFloor = call.request.queryParameters["lowFloor"]
            val wheelchairUse = call.request.queryParameters["wheelchairUse"]

            val actualFrequencies = actualFrequencyService.readActualFrequenciesByDisabilityInfo(
                drivingDate,
                routeNumber,
                lowFloor,
                wheelchairUse
            )

            call.respond(HttpStatusCode.OK, actualFrequencies)
        }

        get("/station") {
            val stationName = call.request.queryParameters["stationName"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]

            val stations = stationService.readStations(
                stationName,
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf)
            )

            call.respond(HttpStatusCode.OK, stations)
        }

        get("/station_disability") {
            val stationName = call.request.queryParameters["stationName"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]
            val accessibility = call.request.queryParameters["accessibility"]
            val waitingAreaSeat = call.request.queryParameters["waitingAreaSeat"]

            val stations = stationService.readStationsByDisabilityInfo(
                stationName,
                routeNumber,
                outboundReturn?.let(OutboundReturnType::valueOf),
                accessibility,
                waitingAreaSeat
            )

            call.respond(HttpStatusCode.OK, stations)
        }

        get("/appointment") {
            val passengerID = call.request.queryParameters["passengerID"]
            val drivingDate = call.request.queryParameters["drivingDate"]

            val appointment = appointmentFormService.readAppointmentForm(passengerID, drivingDate)

            call.respond(HttpStatusCode.OK, appointment)
        }

        post("/passenger") {
            val passenger = call.receive<BodyPassenger>()

            passengerService.addPassenger(passenger)

            call.respond(HttpStatusCode.OK)
        }

        patch("/passenger") {
            val passenger = call.receive<BodyPassenger>()

            passengerService.updatePassenger(passenger)

            call.respond(HttpStatusCode.OK)
        }

        post("/appointment_form") {
            val appointmentForm = call.receive<BodyAppointmentForm>()

            appointmentFormService.addAppointmentForm(appointmentForm)

            call.respond(HttpStatusCode.OK)
        }

        delete("/appointment_form") {
            val appointmentFormId = call.request.queryParameters["appointmentFormId"]

            if (appointmentFormId == null) {
                call.respond(HttpStatusCode.BadRequest, "appointmentFormId is required")
                return@delete
            }

            appointmentFormService.deleteAppointmentForm(appointmentFormId)

            call.respond(HttpStatusCode.OK)
        }

        get("/vmrs") {
            val drivingDate = call.request.queryParameters["drivingDate"]
            val routeNumber = call.request.queryParameters["routeNumber"]

            val vmrs = vehicleMaintenanceRecordService.readVMRS(drivingDate, routeNumber)

            call.respond(HttpStatusCode.OK, vmrs)
        }

        get("/unused_bus") {
            val drivingDate = call.request.queryParameters["drivingDate"]

            val unusedBuses = busService.readUnusedBuses(drivingDate)

            call.respond(HttpStatusCode.OK, unusedBuses)
        }

        post("/bus") {
            val bus = call.receive<BodyBus>()

            busService.addBus(bus)

            call.respond(HttpStatusCode.OK)
        }

        patch("/bus") {
            val bus = call.receive<BodyBus>()

            busService.updateBus(bus)

            call.respond(HttpStatusCode.OK)
        }

        get("/driver") {
            val drivingDate = call.request.queryParameters["drivingDate"]
            val routeNumber = call.request.queryParameters["routeNumber"]
            val departureDate = call.request.queryParameters["departureDate"]
            val licensePlate = call.request.queryParameters["license"]

            val drivers = driverService.readDrivers(drivingDate, routeNumber, departureDate, licensePlate)

            call.respond(HttpStatusCode.OK, drivers)
        }

        post("/driver") {
            val driver = call.receive<BodyDriver>()

            driverService.addDriver(driver)

            call.respond(HttpStatusCode.OK)
        }

        patch("/driver") {
            val driver = call.receive<BodyDriver>()

            driverService.addDriver(driver)

            call.respond(HttpStatusCode.OK)
        }

        get("passenger_phone") {
            val routeNumber = call.request.queryParameters["routeNumber"]
            val outboundReturn = call.request.queryParameters["outboundReturn"]
            val drivingDate = call.request.queryParameters["drivingDate"]
            val departureDate = call.request.queryParameters["departureDate"]

            val phones = passengerService.readPhoneByAppointmentForm(
                routeNumber,
                outboundReturn,
                drivingDate,
                departureDate
            )

            call.respond(HttpStatusCode.OK, phones)
        }
    }
}
