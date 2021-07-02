package gameaccount

sealed class AccountBalanceResponse {
    data class Success(val balance: Int) : AccountBalanceResponse()
    data class Error(val errorMessage: String) : AccountBalanceResponse()
}