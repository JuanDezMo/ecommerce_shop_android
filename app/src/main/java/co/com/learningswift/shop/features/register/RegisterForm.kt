package co.com.learningswift.shop.features.register

import co.com.learningswift.shop.data.model.Country
import co.com.learningswift.shop.data.model.Plan

/** Lo que el usuario ha escrito o elegido, con los valores iniciales del registro */
data class RegisterForm(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val birthDay: Int = 1,
    val birthMonth: Int = 1,
    val birthYear: Int = 2000,
    val country: Country? = null,      // null = "Select"
    val plan: Plan = Plan.BASIC,
    val experienceLevel: Int = 3,
    val newsletter: Boolean = false,
    val termsAccepted: Boolean = false,
)
