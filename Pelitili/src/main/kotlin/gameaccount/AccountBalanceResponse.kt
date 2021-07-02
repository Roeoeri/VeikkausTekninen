package gameaccount

sealed class AccountBalanceResponse {
    data class Success(val balance: Long) : AccountBalanceResponse()
    data class Error(val errorMessage: String) : AccountBalanceResponse()
}