package co.com.learningswift.shop.data.model

/** El `name` (`CARD`, `CASH`, `TRANSFER`) es el valor del backend; `label` es lo que ve el usuario. Nunca se piden datos de tarjeta */
enum class PaymentMethod(val label: String) {
    CARD("Card"),
    CASH("Cash"),
    TRANSFER("Transfer"),
}
