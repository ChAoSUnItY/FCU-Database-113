package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class PassengerService(database: Database) : ServiceBase(database, Passengers) {
    companion object {
        fun passengerByAppointmentFormBuilder(
            routeNumber: String?,
            outboundReturn: String?,
            drivingDate: String?,
            departureTime: String?
        ): String {
            val routeNumberExpr = routeNumber.buildConditionalExpr { "route_number = \"$it\"" }
            val outboundReturnExpr = outboundReturn.buildConditionalExpr { "outbound_return = \"$it\"" }
            val drivingDateExpr = drivingDate.buildConditionalExpr { "driving_date = \"$it\"" }
            val departureTimeExpr = departureTime.buildConditionalExpr { "departure_time = \"$it\"" }

            return """
                select phone
                from Passenger
                where passenger_id in(
                    select passenger_id
                    from AppointmentForms
                    where $routeNumberExpr and $outboundReturnExpr and $drivingDateExpr and $departureTimeExpr
                )
            """
        }
    }

    object Passengers : Table("Passenger") {
        val passengerId = char("passenger_id", 7)
        val passengerName = varchar("passenger_name", 30)
        val gender = bool("gender")
        val phone = integer("phone")
        val mail = varchar("mail", 20)
        val disabilityCategory = integer("disability_category")

        override val primaryKey = PrimaryKey(passengerId)
    }

    suspend fun addPassenger(passenger: BodyPassenger) = dbQuery {
        Passengers.insert {
            it[passengerId] = passenger.passengerId
            it[passengerName] = passenger.passengerName
            it[gender] = passenger.gender
            it[phone] = passenger.phone
            it[mail] = passenger.mail
            it[disabilityCategory] = passenger.disabilityCategory
        }
    }

    suspend fun updatePassenger(passenger: BodyPassenger) = dbQuery {
        Passengers.update({ Passengers.passengerId eq passenger.passengerId }) {
            it[passengerName] = passenger.passengerName
            it[gender] = passenger.gender
            it[phone] = passenger.phone
            it[mail] = passenger.mail
            it[disabilityCategory] = passenger.disabilityCategory
        }
    }

    suspend fun readPhoneByAppointmentForm(
        routeNumber: String?,
        outboundReturn: String?,
        drivingDate: String?,
        departureTime: String?
    ): List<ExposedPhone> = dbQuery {
        queryAndMap(passengerByAppointmentFormBuilder(routeNumber, outboundReturn, drivingDate, departureTime)) {
            ExposedPhone(
                it.getInt("phone").toString()
            )
        }
    }
}
