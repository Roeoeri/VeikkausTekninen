import api.GameAccountApi
import gameaccount.GameAccountDSL
import gameaccount.LocalStamper


fun main() {

    val stamper = LocalStamper()
    val storage = GameAccountDSL(stamper)

    var port: Int = 3000
    when(System.getenv("PORT")){
        is String -> {
            var tryToParsePort = System.getenv("PORT").toIntOrNull()
            when(tryToParsePort){
                is Int -> {
                    port = tryToParsePort
                }
            }
        }
    }

    storage.createPlayerAccount("111qqq", "Pekka", 10000)
    storage.createPlayerAccount("222qqq", "Harri", 20000)
    storage.createPlayerAccount("333qqq", "Jussi", 30000)
    val api = GameAccountApi(port, storage)
    api.startApi()

}
