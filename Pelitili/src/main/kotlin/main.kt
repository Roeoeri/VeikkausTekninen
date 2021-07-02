import api.GameAccountApi
import gameaccount.GameAccountDSL
import gameaccount.LocalStamper


fun main(args: Array<String>) {

    val stamper = LocalStamper()
    val storage = GameAccountDSL(stamper)
    val api = GameAccountApi(3000, storage)
    api.startApi()

}
