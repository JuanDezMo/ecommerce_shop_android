package co.com.learningswift.shop.data.model

/** El `name` (`BASIC`, `PRO`) es el valor que usa el backend; `label` es lo que ve el usuario */
enum class Plan(val label: String) {
    BASIC("Basic"),
    PRO("Pro"),
}
