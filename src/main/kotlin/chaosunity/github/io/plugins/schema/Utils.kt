package chaosunity.github.io.plugins.schema

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.sql.Transaction
import java.sql.ResultSet

fun <T> T?.buildConditionalExpr(pattern: (T?) -> String): String = this?.let(pattern) ?: "true"

fun <T> Transaction.queryAndMap(statement: String, transformation: (ResultSet) -> T): List<T> {
    val list = mutableListOf<T>()

    exec(statement) {
        while (it.next()) {
            list.add(transformation(it))
        }
    }

    return list
}

fun ResultSet.getLocalDate(columnLabel: String): LocalDate = LocalDate.parse(getString(columnLabel))

fun ResultSet.getLocalTime(columnLabel: String): LocalTime = LocalTime.parse(getString(columnLabel))

inline fun <reified T: Enum<T>> ResultSet.getEnum(columnLabel: String): T = enumValueOf<T>(getString(columnLabel))
