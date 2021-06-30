import gameaccountdsl.GameAccountDSL


fun main(args: Array<String>) {
    val dsl=GameAccountDSL()

    val event1 = dsl.depositPlayerAccount("peli1", "11qqq", 200)
    val event2 = dsl.chargePlayerAccount("peli2", "222EEE", 1000)

    println(event1)
    println(event2)
}
