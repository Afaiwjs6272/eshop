package ru.ukhanov.shop.mapper

import org.springframework.stereotype.Component
import ru.ukhanov.shop.dto.UserDto
import ru.ukhanov.shop.model.User

@Component
class UserMapper {
    fun toDto(user: User): UserDto = UserDto(
        id = user.id,
        username = user.username,
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        age = user.age,
        createdAt = user.createdAt,
        updatedAt = user.updatedAt
    )

    fun toEntity(userDto: UserDto): User = User(
        username = userDto.username,
        email = userDto.email,
        firstName = userDto.firstName,
        lastName = userDto.lastName,
        age = userDto.age
    )
}