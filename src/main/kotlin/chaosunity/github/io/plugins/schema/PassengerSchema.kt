package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class PassengerService(database: Database) : ServiceBase(database, Passengers) {
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

    suspend fun updatePassenger(passengerName: String, passengerId: String) = dbQuery {
        Passengers.update({ Passengers.passengerId eq passengerId }) {
            it[Passengers.passengerName] = passengerName
        }
    }
}
