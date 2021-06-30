import io.javalin.Javalin
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.time.LocalDateTime

object PlayerAccount : Table() {
    val id = varchar("id", 250)
    val name = varchar("name", 50)
    val accountBalance = integer("accountBalance")

    override val primaryKey = PrimaryKey(id, name = "PK_Player_ID")
}

object GameEvent : Table() {
    val id = varchar("id", 250)
    val playerId = (varchar("player_id",250) references PlayerAccount.id)
    val timestamp = varchar("timestamp",50)
    val type = varchar("type", 50)
    val amount = integer("amount")

    override val primaryKey = PrimaryKey(id, name = "PK_GameEvent_ID")
}

fun initTables(){
    transaction {
        SchemaUtils.create (PlayerAccount, GameEvent)
    }
}

fun createPlayerAccount(playerId:String, playerName:String, initialBalance:Int){
    transaction {
        PlayerAccount.insert {
            it[id] = playerId
            it[name] = playerName
            it[accountBalance] = initialBalance
        }
    }
}

fun createGameEvent(eventId:String, eventPlayerId: String, eventTimeStamp:String, eventType:String, eventAmount: Int){
    transaction {
        GameEvent.insert {
            it[id]=eventId
            it[playerId]=eventPlayerId
            it[timestamp]=eventTimeStamp
            it[type]=eventType
            it[amount]=eventAmount
        }
    }
}

fun updatePlayerAccountBalance(playerId: String, newBalance: Int){
    transaction {
        PlayerAccount.update ({ PlayerAccount.id eq playerId }) {
            it[PlayerAccount.accountBalance] = newBalance
        }
    }
}

fun gameEventExists(gameEventId: String): Boolean {
    val previousEventCount = transaction {
        GameEvent.select {GameEvent.id eq gameEventId}.count()
    }
    if(previousEventCount > 0) return  true
    return false
}

fun playerAccountExists(playerId: String): Boolean {

    val accountsWithGivenId = transaction {
        PlayerAccount.select {PlayerAccount.id eq playerId}.count()
    }
    if(accountsWithGivenId < 1) return false
    return true
}

fun getPlayerAccountBalance(playerId: String): Int {
    if(playerAccountExists(playerId)){
        return transaction {
            PlayerAccount.select {PlayerAccount.id eq playerId}.single()[PlayerAccount.accountBalance]
        }
    }
    throw Exception("Player with this id does not exist")
}

fun chargePlayerAccount(gameEventId:String, playerId: String, amount:Int): Int{
    if(gameEventExists(gameEventId)) throw Exception("Event with same id already exists")
    if(!playerAccountExists(playerId)) throw Exception("Player with this id does not exist")
    if(getPlayerAccountBalance(playerId) < amount) throw Exception("Account does not have enough balance to complete transaction")

    val currentBalance = getPlayerAccountBalance(playerId)
    val newBalance = currentBalance - amount
    val timeStamp = LocalDateTime.now().toString()

    createGameEvent(gameEventId, playerId, timeStamp, "charge", amount)
    updatePlayerAccountBalance(playerId, newBalance)

    return newBalance
}

fun depositPlayerAccount(gameEventId:String, playerId: String, amount:Int): Int{
    if(gameEventExists(gameEventId)) throw Exception("Event with same id already exists")
    if(!playerAccountExists(playerId)) throw Exception("Player with this id does not exist")

    val currentBalance = getPlayerAccountBalance(playerId)
    val newBalance = currentBalance + amount
    val timeStamp = LocalDateTime.now().toString()

    createGameEvent(gameEventId, playerId, timeStamp, "deposit", amount)
    updatePlayerAccountBalance(playerId, newBalance)

    return newBalance
}

fun main(args: Array<String>) {

    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "root", password = "")
    initTables()
    createPlayerAccount("11qqq", "Pekka", 2000)
    createPlayerAccount("222EEE", "Jussi", 5000)
    createPlayerAccount("333RRR", "Kari", 10000)

    depositPlayerAccount("peli1", "11qqq", 200)
    chargePlayerAccount("peli2", "222EEE", 1000)


    transaction {
        for (account in PlayerAccount.selectAll()) {
            println(account)
        }

        for (event in GameEvent.selectAll()) {
            println(event)
        }
    }

}
