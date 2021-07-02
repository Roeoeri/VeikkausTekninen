package gameaccount
import java.time.LocalDateTime


class LocalStamper:TimeStamper {
    override fun getTimeStamp(): LocalDateTime {
        return LocalDateTime.now()
    }
}