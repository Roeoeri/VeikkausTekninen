package gameaccountdsl

import model.PlayerAccount
import model.GameEvent
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.time.LocalDateTime

class GameAccountDSL() {

    init{
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "root", password = "")
        initTables()
    }

    private fun initTables(){
        transaction {
            SchemaUtils.create (PlayerAccount, GameEvent)
        }
    }

    public fun createPlayerAccount(playerId:String, playerName:String, initialBalance:Int){
        transaction {
            PlayerAccount.insert {
                it[id] = playerId
                it[name] = playerName
                it[accountBalance] = initialBalance
            }
        }
    }

    private fun createGameEvent(eventId:String, eventPlayerId: String, eventTimeStamp:String, eventType:String, eventAmount: Int){
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

    private fun updatePlayerAccountBalance(playerId: String, newBalance: Int){
        transaction {
            PlayerAccount.update ({ PlayerAccount.id eq playerId }) {
                it[PlayerAccount.accountBalance] = newBalance
            }
        }
    }

    private fun gameEventExists(gameEventId: String): Boolean {
        val previousEventCount = transaction {
            GameEvent.select {GameEvent.id eq gameEventId}.count()
        }
        if(previousEventCount > 0) return  true
        return false
    }

    private fun playerAccountExists(playerId: String): Boolean {

        val accountsWithGivenId = transaction {
            PlayerAccount.select {PlayerAccount.id eq playerId}.count()
        }
        if(accountsWithGivenId < 1) return false
        return true
    }

    private fun getPlayerAccountBalance(playerId: String): Int {
            return transaction {
                PlayerAccount.select {PlayerAccount.id eq playerId}.single()[PlayerAccount.accountBalance]
            }
    }

    fun chargePlayerAccount(gameEventId:String, playerId: String, amount:Int): AccountBalanceResponse{
        if(gameEventExists(gameEventId)) return AccountBalanceResponse.Error("Event with same id already exists")
        if(!playerAccountExists(playerId)) return  AccountBalanceResponse.Error("Player with this id does not exist")
        if(getPlayerAccountBalance(playerId) < amount) return AccountBalanceResponse.Error("Account does not have enough balance to complete transaction")

        val currentBalance = getPlayerAccountBalance(playerId)
        val newBalance = currentBalance - amount
        val timeStamp = LocalDateTime.now().toString()

        createGameEvent(gameEventId, playerId, timeStamp, "charge", amount)
        updatePlayerAccountBalance(playerId, newBalance)

        return AccountBalanceResponse.Success(newBalance)
    }

    fun depositPlayerAccount(gameEventId:String, playerId: String, amount:Int): AccountBalanceResponse{
        if(gameEventExists(gameEventId)) return AccountBalanceResponse.Error("Event with same id already exists")
        if(!playerAccountExists(playerId)) return  AccountBalanceResponse.Error("Player with this id does not exist")

        val currentBalance = getPlayerAccountBalance(playerId)
        val newBalance = currentBalance + amount
        val timeStamp = LocalDateTime.now().toString()

        createGameEvent(gameEventId, playerId, timeStamp, "deposit", amount)
        updatePlayerAccountBalance(playerId, newBalance)

        return AccountBalanceResponse.Success(newBalance)
    }
}