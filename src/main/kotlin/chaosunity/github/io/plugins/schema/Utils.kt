package chaosunity.github.io.plugins.schema

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.ResultSet

fun <T> T?.buildConditionalExpr(pattern: (T?) -> String): String = this?.let(pattern) ?: "true"

fun <T> Transaction.queryAndMap(statement: String, transformation: (ResultSet) -> T): List<T> {
    val list = mutableListOf<T>()

    transaction {
        exec(statement) {
            while (it.next()) {
                list.add(transformation(it))
            }
        }
    }

    return list
}
