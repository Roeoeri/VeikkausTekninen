package model

import org.jetbrains.exposed.sql.Table

object PlayerAccount : Table() {
    val id = varchar("id", 250)
    val name = varchar("name", 50)
    val accountBalance = integer("accountBalance")

    override val primaryKey = PrimaryKey(id, name = "PK_Player_ID")
}