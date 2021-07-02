package gameaccount

interface GameAccount {

    fun chargePlayerAccount(gameEventId:String, playerId: String, amount:Int): AccountBalanceResponse
    fun depositPlayerAccount(gameEventId:String, playerId: String, amount:Int): AccountBalanceResponse

}