package model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object GameEvent : Table() {
    val id = varchar("id", 250)
    val playerId = (varchar("player_id",250) references PlayerAccount.id)
    val timestamp = datetime("timestamp")
    val type = varchar("type", 50)
    val amount = integer("amount")

    override val primaryKey = PrimaryKey(id, name = "PK_GameEvent_ID")
}

