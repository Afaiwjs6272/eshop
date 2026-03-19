package ru.ukhanov.shop.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserDto(
    val id: Long? = null,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Min(value = 0, message = "Age must be greater than or equal to 0")
    val age: Int,

    val role: String = "USER",

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val createdAt: LocalDateTime? = null,

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val updatedAt: LocalDateTime? = null
)