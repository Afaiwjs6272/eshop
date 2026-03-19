package ru.ukhanov.shop.service.user

import org.springframework.data.domain.Page
import ru.ukhanov.shop.dto.UserDto

interface UserService {
    fun createUser(userDto: UserDto): UserDto

    fun getUserById(id: Long): UserDto

    fun getAllUsers(page: Int, size: Int): Page<UserDto>

    fun updateUser(id: Long, userDto: UserDto): UserDto

    fun deleteUser(id: Long)

    fun searchUsersByName(name: String): List<UserDto>

    fun getUsersByMinAge(minAge: Int): List<UserDto>
}