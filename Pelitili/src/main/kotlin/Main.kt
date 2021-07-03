import api.GameAccountApi
import gameaccount.GameAccountDSL
import gameaccount.LocalStamper


fun main() {

    val stamper = LocalStamper()
    val storage = GameAccountDSL(stamper)

    val parsePort = System.getenv("PORT").toIntOrNull()
    var port: Int = 3000
    
    when(parsePort){
        is Int -> {port = parsePort}
        
    }

    storage.createPlayerAccount("111qqq", "Pekka", 10000)
    storage.createPlayerAccount("222qqq", "Harri", 20000)
    storage.createPlayerAccount("333qqq", "Jussi", 30000)
    val api = GameAccountApi(port, storage)
    api.startApi()

}
