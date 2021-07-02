package model

import org.jetbrains.exposed.sql.Table

object PlayerAccount : Table() {
    val id = varchar(ModelSettings.playerIdName, ModelSettings.playerIdLength)
    val name = varchar(ModelSettings.playerNameName, ModelSettings.playerNameLength)
    val accountBalance = long(ModelSettings.playerAccountBalanceName)

    override val primaryKey = PrimaryKey(id, name = "PK_Player_ID")
}