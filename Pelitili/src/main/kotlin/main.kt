import api.GameAccountApi
import gameaccount.GameAccountDSL
import gameaccount.LocalStamper


fun main(args: Array<String>) {

    val stamper = LocalStamper()
    val storage = GameAccountDSL(stamper)
    storage.createPlayerAccount("111qqq", "Pekka", 10000)
    storage.createPlayerAccount("222qqq", "Harri", 20000)
    storage.createPlayerAccount("333qqq", "Jussi", 30000)
    val api = GameAccountApi(3000, storage)
    api.startApi()

}
