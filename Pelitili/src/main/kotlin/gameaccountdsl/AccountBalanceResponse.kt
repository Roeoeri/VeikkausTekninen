package gameaccountdsl

sealed class AccountBalanceResponse {
    data class Success(val balance: Int) : AccountBalanceResponse()
    data class Error(val ErrorMessage: String) : AccountBalanceResponse()
}