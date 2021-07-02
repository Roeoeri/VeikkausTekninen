package gameaccount

interface GameAccount {

    fun chargePlayerAccount(gameEventId:String, playerId: String, amount:Long): AccountBalanceResponse
    fun depositPlayerAccount(gameEventId:String, playerId: String, amount:Long): AccountBalanceResponse

    //for illustration purposes
    fun getPlayerAccounts():List<String>
    fun getGameEvents():List<String>

}