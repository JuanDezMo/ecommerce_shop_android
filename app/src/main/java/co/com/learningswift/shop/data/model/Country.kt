package co.com.learningswift.shop.data.model

/** Los seis países que ofrece la app. `code` es lo que se envía al backend (el backend acepta también `ES`, pero no se ofrece) */
enum class Country(val code: String, val displayName: String) {
    AR("AR", "Argentina"),
    CL("CL", "Chile"),
    CO("CO", "Colombia"),
    MX("MX", "Mexico"),
    PE("PE", "Peru"),
    US("US", "United States");

    companion object {
        val sorted: List<Country> = entries.sortedBy { it.displayName }
    }
}
