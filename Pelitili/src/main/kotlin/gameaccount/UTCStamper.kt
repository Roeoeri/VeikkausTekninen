package gameaccount
import java.time.Instant


class UTCStamper:TimeStamper {
    override fun getTimeStamp(): String {
        return Instant.now().toString()
    }
}