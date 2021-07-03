package gameaccount

import model.GameEvent
import model.PlayerAccount
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import utils.ErrorMessages
import utils.EventTypes
import java.time.LocalDateTime

internal class GameAccountDSLTest {

    class MockTimeStamper: TimeStamper {
        override fun getTimeStamp(): LocalDateTime {
            return LocalDateTime.parse("2007-12-03T10:15:30")
        }
    }
    val mockTimeStamper = MockTimeStamper()

    private val dsl = GameAccountDSL(mockTimeStamper)
    private val PLAYER_ID = "222eee"
    private val INITIAL_ACCOUNT_BALANCE: Long = 2000
    private val INITIAL_PLAYER_NAME = "Jussi"

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

        addTestPlayerAccount()
        val foundPlayer = getPlayerAccount(PLAYER_ID)

        assertEquals(PLAYER_ID,foundPlayer[PlayerAccount.id])
        assertEquals(INITIAL_PLAYER_NAME,foundPlayer[PlayerAccount.name])
        assertEquals(INITIAL_ACCOUNT_BALANCE,foundPlayer[PlayerAccount.accountBalance])
    }

    @DisplayName("DSL does not create multiple accounts with same playerId")
    @Test
    fun doesNotCreateDuplicates(){
        addTestPlayerAccount()
        val name = "Pekka"
        val initialAccountBalance: Long = 3000
        dsl.createPlayerAccount(PLAYER_ID,name,initialAccountBalance)

        assertEquals(1, getNumberOfPlayerAccountsWithId(PLAYER_ID))
    }

    @DisplayName("Charging player account twice with same gameEventID returns an error on second charge")
    @Test
    fun returnsErrorWhenChargedTwice(){
        addTestPlayerAccount()

        val gameEventId = "peli13"
        val amount:Long = 1000

        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val secondRes = dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)
        val expectedErrorMessage = ErrorMessages.eventIdExists

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
        val amount:Long = 1000

        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        val expectedBalance = INITIAL_ACCOUNT_BALANCE - 1
        assertEquals(expectedBalance, accountBalance)
    }

    @DisplayName("Charging nonexistent playeraccount returns correct error")
    @Test
    fun chargingNonexistentPlayer(){
        val gameEventId = "peli13"
        val amount:Long = 1000
        val nonexistentPlayerID = "peli200"
        val res = dsl.chargePlayerAccount(gameEventId,nonexistentPlayerID,amount)

        val expectedErrorMessage = ErrorMessages.playerIdDoesNotExist

        when(res){
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(res.errorMessage, expectedErrorMessage)
        }

    }

    @DisplayName("Charging nonexistent playeraccount does not create GameEvent")
    @Test
    fun chargingNonexistentPlayerDoesNotCreateGameEvent(){
        val gameEventId = "peli13"
        val amount:Long = 1000
        val nonexistentPlayerID = "peli200"
        dsl.chargePlayerAccount(gameEventId,nonexistentPlayerID,amount)

        assertEquals(0,getNumberOFGameEventsWithID(gameEventId))

    }

    @DisplayName("Charging playeraccount without enough balance returns correct error")
    @Test
    fun chargingTooMuchReturnsError(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount:Long = 3000
        val res = dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val expectedErrorMessage = ErrorMessages.notEnoughBalance

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
        val amount:Long = 3000
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
        val amount:Long = 3000
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val numberOfGameEventsWithThisId = getNumberOFGameEventsWithID(gameEventId)

        assertEquals(0, numberOfGameEventsWithThisId)
    }


    @DisplayName("Charging playeraccount with enough balance succeeds")
    @Test
    fun chargingPlayerAccountWithEnoughBalanceSucceeds(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount: Long  = 1000
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
        val amount:Long = 1000

        val timeStamp = mockTimeStamper.getTimeStamp()
        dsl.chargePlayerAccount(gameEventId,PLAYER_ID,amount)

        val gameEvent = getGameEvent(gameEventId)

        assertEquals(timeStamp, gameEvent[GameEvent.timestamp])
        assertEquals(gameEventId, gameEvent[GameEvent.id])
        assertEquals(PLAYER_ID, gameEvent[GameEvent.playerId])
        assertEquals(EventTypes.charge, gameEvent[GameEvent.type])
        assertEquals(amount, gameEvent[GameEvent.amount])
    }

    @DisplayName("Depositing to nonexistent playeraccount returns correct error")
    @Test
    fun depositingToNonexistentPlayer(){
        val gameEventId = "peli13"
        val amount:Long = 1000
        val nonexistentPlayerID = "peli200"
        val res = dsl.depositPlayerAccount(gameEventId,nonexistentPlayerID,amount)

        val expectedErrorMessage = ErrorMessages.playerIdDoesNotExist

        when(res){
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(res.errorMessage, expectedErrorMessage)
        }

    }

    @DisplayName("Depositing to nonexistent playeraccount does not create GameEvent")
    @Test
    fun depositingToNonexistentPlayerDoesNotCreateGameEvent(){
        val gameEventId = "peli13"
        val amount:Long = 1000
        val nonexistentPlayerID = "peli200"
        dsl.depositPlayerAccount(gameEventId,nonexistentPlayerID,amount)

        assertEquals(0,getNumberOFGameEventsWithID(gameEventId))

    }

    @DisplayName("Depositing to player account twice with same gameEventID returns an error on second charge")
    @Test
    fun returnsErrorWhenDepositedTwice(){
        addTestPlayerAccount()

        val gameEventId = "peli13"
        val amount: Long = 1000

        dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)

        val secondRes = dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)
        val expectedErrorMessage = ErrorMessages.eventIdExists

        when(secondRes) {
            is AccountBalanceResponse.Success -> assertFalse(true)
            is AccountBalanceResponse.Error -> assertEquals(secondRes.errorMessage, expectedErrorMessage)
        }
    }

    @DisplayName("Depositing player account twice with same gameEventID adds only the first payment to account balance")
    @Test
    fun doesNotAddBalanceTwice(){
        addTestPlayerAccount()

        val gameEventId = "peli13"
        val amount: Long = 1000

        dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)
        dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        val expectedBalance = INITIAL_ACCOUNT_BALANCE + amount
        assertEquals(expectedBalance, accountBalance)
    }

    @DisplayName("Depositing to playeraccount succeeds")
    @Test
    fun depositingSuccees(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount: Long = 1000
        val res = dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)

        val expectedAccountBalance = INITIAL_ACCOUNT_BALANCE + amount

        val accountBalance = getPlayerAccount(PLAYER_ID)[PlayerAccount.accountBalance]
        assertEquals(expectedAccountBalance, accountBalance)

        when(res){
            is AccountBalanceResponse.Error -> assertFalse(true)
            is AccountBalanceResponse.Success -> assertEquals(expectedAccountBalance, res.balance)

        }
    }

    @DisplayName("Depositing to playeraccount created correct gameEvent")
    @Test
    fun depositingCreatesCorrectGameEvent(){
        addTestPlayerAccount()
        val gameEventId = "peli13"
        val amount: Long = 1000

        val timeStamp = mockTimeStamper.getTimeStamp()
        dsl.depositPlayerAccount(gameEventId,PLAYER_ID,amount)

        val gameEvent = getGameEvent(gameEventId)

        assertEquals(timeStamp, gameEvent[GameEvent.timestamp])
        assertEquals(gameEventId, gameEvent[GameEvent.id])
        assertEquals(PLAYER_ID, gameEvent[GameEvent.playerId])
        assertEquals(EventTypes.deposit, gameEvent[GameEvent.type])
        assertEquals(amount, gameEvent[GameEvent.amount])
    }


    @AfterEach
    fun tearDown() {
        clearDatabase()
    }


}