package gameaccountdsl

import model.GameEvent
import model.PlayerAccount
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.notExists
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class GameAccountDSLTest {

    val dsl = GameAccountDSL()
    val PLAYER_ID = "222eee"
    val INITIAL_ACCOUNT_BALANCE = 2000
    val INITIAL_PLAYER_NAME = "Jussi"

    private fun clearDatabase(){
        transaction {
            SchemaUtils.drop(PlayerAccount, GameEvent)
            SchemaUtils.create(PlayerAccount, GameEvent)
        }
    }

    private fun getPlayerAccount(playerId: String):ResultRow{
        return transaction { PlayerAccount.select { PlayerAccount.id eq playerId }.single() }
    }

    private fun getNumberOfPlayerAccountsWithId(playerId: String):Long{
        return transaction { PlayerAccount.select { PlayerAccount.id eq playerId }.count() }
    }

    private fun getGameEvent(eventId: String):ResultRow{
        return transaction { GameEvent.select { GameEvent.id eq eventId }.single() }
    }

    private fun getNumberOFGameEventsWithID(eventId: String):Long{
        return transaction { GameEvent.select { GameEvent.id eq eventId }.count() }
    }

    private fun addTestPlayerAccount(){
        val playerID=PLAYER_ID
        val name = INITIAL_PLAYER_NAME
        val initialAccountBalance = INITIAL_ACCOUNT_BALANCE

        dsl.createPlayerAccount(playerID,name,initialAccountBalance)
    }

    @DisplayName("DSL creates player account")
    @Test
    fun createsPlayerAccount(){
        val playerID="222eee"
        val name = "Jussi"
        val initialAccountBalance = 2000

        dsl.createPlayerAccount(playerID,name,initialAccountBalance)
        val foundPlayer = getPlayerAccount(playerID)

        assertEquals(playerID,foundPlayer[PlayerAccount.id])
        assertEquals(name,foundPlayer[PlayerAccount.name])
        assertEquals(initialAccountBalance,foundPlayer[PlayerAccount.accountBalance])
    }

    @DisplayName("DSL does not create multiple accounts with same playerId")
    @Test
    fun doesNotCreateDuplicates(){
        addTestPlayerAccount()
        val playerID="222eee"
        val name = "Pekka"
        val initialAccountBalance = 3000
        dsl.createPlayerAccount(playerID,name,initialAccountBalance)

        assertEquals(1, getNumberOfPlayerAccountsWithId(playerID))
    }

    @DisplayName("Charging player account twice with same gameEventID returns an error on second charge")
    @Test
    fun returnsErrorWhenChargedTwice(){
        addTestPlayerAccount()

        val gameEventId = "peli13"
        val amount = 1000

        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val secondRes = dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)
        val expectedErrorMessage = "Event with same id already exists"

        when(secondRes) {
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(secondRes.errorMessage, expectedErrorMessage)
        }
    }

    @DisplayName("Charging player account twice with same gameEventID subtracts only the first payment from account balance")
    @Test
    fun doesNotSubtractBalanceTwice(){
        addTestPlayerAccount()

        val gameEventId = "peli13"
        val amount = 1000

        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        val expectedBalance = INITIAL_ACCOUNT_BALANCE - amount
        assertEquals(expectedBalance, accountBalance)
    }

    @DisplayName("Charging nonexistent playeraccount returns correct error")
    @Test
    fun chargingNonexistentPlayer(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 1000
        val nonexistentPlayerID = "peli200"
        val res = dsl.chargePlayerAccount(gameEventId,nonexistentPlayerID,amount)

        val expectedErrorMessage = "Player with this id does not exist"

        when(res){
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(res.errorMessage, expectedErrorMessage)
        }

    }

    @DisplayName("Charging playeraccount without enough balance returns correct error")
    @Test
    fun chargingTooMuchReturnsError(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 3000
        val res = dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val expectedErrorMessage = "Account does not have enough balance to complete transaction"

        when(res){
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(res.errorMessage, expectedErrorMessage)
        }

    }

    @DisplayName("Charging playeraccount without enough balance does not alter the balance")
    @Test
    fun chargingTooMuchDoesNotAlterBalancer(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 3000
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        assertEquals(INITIAL_ACCOUNT_BALANCE, accountBalance)
        val expectedBalance = INITIAL_ACCOUNT_BALANCE

        assertEquals(expectedBalance, accountBalance)

    }

    @DisplayName("Charging playeraccount without enough balance does not create GameEvent")
    @Test
    fun chargingTooMuchDoesNotCreateGameEvent(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 3000
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val numberOfGameEventsWithThisId = getNumberOFGameEventsWithID(gameEventId)

        assertEquals(0, numberOfGameEventsWithThisId)
    }


    @DisplayName("Charging playeraccount with enough balance succeeds")
    @Test
    fun chargingPlayerAccountWithEnoughBalanceSucceeds(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 1000
        val res = dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val expectedAccountBalance = INITIAL_ACCOUNT_BALANCE - amount

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        assertEquals(expectedAccountBalance, accountBalance)

        when(res){
            is AccountBalanceResponse.Error -> assertFalse(true)
            is AccountBalanceResponse.Success -> assertEquals(expectedAccountBalance, res.balance)

        }
    }

    @DisplayName("Charging playeraccount with enough balance creates correct GameEvent")
    @Test
    fun chargingPlayerAccountWithEnoughBalanceCreatesGameEvent(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount = 1000

        val timeStamp = LocalDateTime.now().toString()
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val gameEvent = getGameEvent(gameEventId)

        assertEquals(timeStamp, gameEvent[GameEvent.timestamp])

    }


    @AfterEach
    fun tearDown() {
        clearDatabase()
    }


}