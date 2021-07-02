package model

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object GameEvent : Table() {
    val id = varchar(ModelSettings.gameEventIdName, ModelSettings.gameEventIdLength)
    val playerId = (varchar(ModelSettings.gameEventPlayerIdName,ModelSettings.playerIdLength) references PlayerAccount.id)
    val timestamp = datetime(ModelSettings.gameEventTimeStampName)
    val type = varchar(ModelSettings.gameEventTypeName, ModelSettings.gameEventTypeLength)
    val amount = long(ModelSettings.gameEventAmountName)

    override val primaryKey = PrimaryKey(id, name = "PK_GameEvent_ID")
}

