package gameaccount

import model.PlayerAccount
import model.GameEvent
import model.ModelSettings
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.ErrorMessages
import utils.EventTypes
import java.time.LocalDateTime

class GameAccountDSL(private val timeStamper: TimeStamper): GameAccount {

    init{
        //if there is Database url, assume it is to Heroku postgress and try to connect
        when(System.getenv("DATABASE_URL")) {
            is String -> {
                val key = System.getenv("DATABASE_URL")

                val uri = "jdbc:postgresql://${key.split(("@"))[1]}"
                val credential = key.split("@")[0].removePrefix("postgres://")

                val username = credential.split(":")[0]
                val password = credential.split(":")[1]

                Database.connect(uri, "org.postgresql.Driver", username, password)
                initTablesInProduction()

            }
            else -> {
                Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver", user = "root", password = "")
                initTables()
            }
        }
    }

    private fun initTables(){
        transaction {
            SchemaUtils.create (PlayerAccount, GameEvent)
        }
    }

    private fun initTablesInProduction(){

        transaction {
            SchemaUtils.createMissingTablesAndColumns (PlayerAccount, GameEvent)
        }

    }

    fun createPlayerAccount(playerId:String, playerName:String, initialBalance:Long){
        if(!playerAccountExists(playerId)){
            transaction {
                PlayerAccount.insert {
                    it[id] = playerId
                    it[name] = playerName
                    it[accountBalance] = initialBalance
                }
            }
        }
    }

    private fun createGameEvent(eventId:String, eventPlayerId: String, eventTimeStamp:LocalDateTime, eventType:String, eventAmount: Long){
        transaction {
            GameEvent.insert {
                it[id]=eventId
                it[playerId]=eventPlayerId
                it[timestamp]= eventTimeStamp
                it[type]=eventType
                it[amount]=eventAmount
            }
        }
    }

    private fun updatePlayerAccountBalance(playerId: String, newBalance: Long){
        transaction {
            PlayerAccount.update ({ PlayerAccount.id eq playerId }) {
                it[accountBalance] = newBalance
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

    private fun getPlayerAccountBalance(playerId: String): Long {
            return transaction {
                PlayerAccount.select {PlayerAccount.id eq playerId}.single()[PlayerAccount.accountBalance]
            }
    }

    override fun chargePlayerAccount(gameEventId:String, playerId: String, amount:Long): AccountBalanceResponse{
        if(gameEventExists(gameEventId)) return AccountBalanceResponse.Error(ErrorMessages.eventIdExists)
        if(!playerAccountExists(playerId)) return  AccountBalanceResponse.Error(ErrorMessages.playerIdDoesNotExist)
        if(getPlayerAccountBalance(playerId) < amount) return AccountBalanceResponse.Error(ErrorMessages.notEnoughBalance)
        if(gameEventId.length > ModelSettings.gameEventIdLength) return AccountBalanceResponse.Error(ErrorMessages.gameEventIdTooLong)

        val currentBalance = getPlayerAccountBalance(playerId)
        val newBalance = currentBalance - amount
        val timeStamp = timeStamper.getTimeStamp()

        createGameEvent(gameEventId, playerId, timeStamp, EventTypes.charge, amount)
        updatePlayerAccountBalance(playerId, newBalance)

        return AccountBalanceResponse.Success(newBalance)
    }

    override fun depositPlayerAccount(gameEventId:String, playerId: String, amount:Long): AccountBalanceResponse{
        if(gameEventExists(gameEventId)) return AccountBalanceResponse.Error(ErrorMessages.eventIdExists)
        if(!playerAccountExists(playerId)) return  AccountBalanceResponse.Error(ErrorMessages.playerIdDoesNotExist)
        if(gameEventId.length > ModelSettings.gameEventIdLength) return AccountBalanceResponse.Error(ErrorMessages.gameEventIdTooLong)

        val currentBalance = getPlayerAccountBalance(playerId)
        val newBalance = currentBalance + amount
        val timeStamp = timeStamper.getTimeStamp()

        createGameEvent(gameEventId, playerId, timeStamp, EventTypes.deposit, amount)
        updatePlayerAccountBalance(playerId, newBalance)

        return AccountBalanceResponse.Success(newBalance)
    }

    override fun getPlayerAccounts(): List<String>{
        val list = mutableListOf<String>()
        transaction {
            for(player in PlayerAccount.selectAll()){
                list.add(player.toString())
            }
        }

        return list
    }

    override fun getGameEvents(): List<String>{
        val list = mutableListOf<String>()
        transaction {
           for(event in GameEvent.selectAll()){
               list.add(event.toString())
           }
        }
        return list
    }

}