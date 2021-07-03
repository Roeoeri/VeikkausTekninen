package utils

object ErrorMessages {
    const val eventIdExists = "Event with same id already exists"
    const val playerIdDoesNotExist = "Player with this id does not exist"
    const val notEnoughBalance = "Account does not have enough balance to complete transaction"
    const val gameEventIdTooLong = "GameEvent Id is too long"
    const val malformatedParameters = "JSON in post message was invalid. Check the documentation for correct format"
}