package api
import gameaccount.AccountBalanceResponse
import gameaccount.GameAccount
import io.javalin.Javalin

class GameAccountApi(val port: Int, val storage: GameAccount) {

    data class DepositOrChargeRequest(val gameEventId:String, val playerId: String, val amount: Int)
    data class ErrorResponse(val message:String)
    data class SuccessResponse(val balance:Int)

    fun startApi(){
        val app  = Javalin.create().start(port)
        app.get("/players") { ctx ->
            println("Asd")
            ctx.json(storage.getPlayerAccounts())}
        app.get("/gameEvents") { ctx -> ctx.json(storage.getGameEvents())}

        app.post("/api/deposit"){ ctx ->
            val params = ctx.body<DepositOrChargeRequest>()
            when(val transactionEvent = storage.depositPlayerAccount(params.gameEventId, params.playerId, params.amount)){
                is AccountBalanceResponse.Error -> {
                    ctx.status(400)
                    ctx.json(ErrorResponse(transactionEvent.errorMessage))

                }
                is AccountBalanceResponse.Success -> {
                    ctx.status(200)
                    ctx.json(SuccessResponse(transactionEvent.balance))
                }
            }
        }

        app.post("/api/charge"){ ctx ->
            val params = ctx.body<DepositOrChargeRequest>()
            when(val transactionEvent = storage.chargePlayerAccount(params.gameEventId, params.playerId, params.amount)){
                is AccountBalanceResponse.Error -> {
                    ctx.status(400)
                    ctx.json(ErrorResponse(transactionEvent.errorMessage))

                }
                is AccountBalanceResponse.Success -> {
                    ctx.status(200)
                    ctx.json(SuccessResponse(transactionEvent.balance))
                }
            }
        }

    }

}