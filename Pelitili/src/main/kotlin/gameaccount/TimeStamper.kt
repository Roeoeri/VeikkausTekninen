package gameaccount

import java.time.LocalDateTime

interface TimeStamper {

    fun getTimeStamp():LocalDateTime
}