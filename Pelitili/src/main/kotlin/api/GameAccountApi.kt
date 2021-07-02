package api
import gameaccount.GameAccount
import io.javalin.Javalin

class GameAccountApi(port:Int, storage: GameAccount) {

    val port = port

    fun startApi(){
        val app  = Javalin.create().start(port)
        app.get("/") { ctx -> ctx.result("Hello World")}
    }

}