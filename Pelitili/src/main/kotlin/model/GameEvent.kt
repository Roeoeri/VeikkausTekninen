package model

import org.jetbrains.exposed.sql.Table

object GameEvent : Table() {
    val id = varchar("id", 250)
    val playerId = (varchar("player_id",250) references PlayerAccount.id)
    val timestamp = varchar("timestamp",50)
    val type = varchar("type", 50)
    val amount = integer("amount")

    override val primaryKey = PrimaryKey(id, name = "PK_GameEvent_ID")
}

