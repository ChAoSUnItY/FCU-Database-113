package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

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
}
