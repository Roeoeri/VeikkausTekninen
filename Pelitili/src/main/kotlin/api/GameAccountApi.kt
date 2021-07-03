package api
import gameaccount.AccountBalanceResponse
import gameaccount.GameAccount
import io.javalin.Javalin
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context

class GameAccountApi(private val port: Int, private val storage: GameAccount) {

    data class DepositOrChargeRequest(val gameEventId:String, val playerId: String, val amount: Long)
    data class SuccessResponse(val balance:Long)

    private fun handleTransActionEvent(transactionEvent: AccountBalanceResponse, ctx: Context) {

        when(transactionEvent){
            is AccountBalanceResponse.Error -> {
                throw BadRequestResponse(transactionEvent.errorMessage)
            }
            is AccountBalanceResponse.Success -> {
                ctx.status(200)
                ctx.json(SuccessResponse(transactionEvent.balance))
            }
        }
    }

    fun startApi(){
        val app  = Javalin.create().start(port)
        app.get("/players") { ctx -> ctx.json(storage.getPlayerAccounts())}

        app.get("/gameEvents") { ctx -> ctx.json(storage.getGameEvents())}

        app.post("/api/deposit"){ ctx ->
            val params = ctx.bodyValidator<DepositOrChargeRequest>().get()
            val transactionEvent = storage.depositPlayerAccount(params.gameEventId, params.playerId, params.amount)
            handleTransActionEvent(transactionEvent,ctx)
        }

        app.post("/api/charge") { ctx ->
            val params = ctx.bodyValidator<DepositOrChargeRequest>().get()
            val transactionEvent = storage.chargePlayerAccount(params.gameEventId, params.playerId, params.amount)
            handleTransActionEvent(transactionEvent, ctx)
        }
    }
}