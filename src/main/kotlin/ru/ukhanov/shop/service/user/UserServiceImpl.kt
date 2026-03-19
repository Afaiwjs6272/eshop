package ru.ukhanov.shop.service.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ukhanov.shop.annotation.MonitorPerformance
import ru.ukhanov.shop.dto.UserDto
import ru.ukhanov.shop.dto.UserEvent
import ru.ukhanov.shop.dto.enums.UserEventType
import ru.ukhanov.shop.exception.DuplicateResourceException
import ru.ukhanov.shop.exception.ResourceNotFoundException
import ru.ukhanov.shop.mapper.UserMapper
import ru.ukhanov.shop.repository.UserRepository
import ru.ukhanov.shop.service.KafkaProducerService
import java.time.LocalDateTime
import java.util.*

@Service
class UserServiceImpl (
    private val userRepository: UserRepository,
    private val kafkaProducerService: KafkaProducerService,
    private val userMapper: UserMapper
): UserService {

    @Transactional
    @MonitorPerformance
    override fun createUser(userDto: UserDto): UserDto {
        if (userRepository.existsByEmail(userDto.email)) {
            throw DuplicateResourceException("User with email ${userDto.email} already exists")
        }
        if (userRepository.existsByUsername(userDto.username)) {
            throw DuplicateResourceException("User with username ${userDto.username} already exists")
        }

        val user = userMapper.toEntity(userDto)
        val savedUser = userRepository.save(user)

        kafkaProducerService.sendUserEvent(
            UserEvent(
                eventId = UUID.randomUUID().toString(),
                userId = savedUser.id!!,
                userEmail = savedUser.email,
                eventType = UserEventType.CREATED,
                userData = userMapper.toDto(savedUser)
            )
        )

        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    @MonitorPerformance
    override fun getUserById(id: Long): UserDto {
        return userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }
            .let { userMapper.toDto(it) }
    }

    @Transactional(readOnly = true)
    @MonitorPerformance
    override fun getAllUsers(page: Int, size: Int): Page<UserDto> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return userRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map { userMapper.toDto(it) }
    }

    @Transactional
    @MonitorPerformance
    override fun updateUser(id: Long, userDto: UserDto): UserDto {
        val existingUser = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        if (existingUser.email != userDto.email && userRepository.existsByEmail(userDto.email)) {
            throw DuplicateResourceException("User with email ${userDto.email} already exists")
        }

        if (existingUser.username != userDto.username && userRepository.existsByUsername(userDto.username)) {
            throw DuplicateResourceException("User with username ${userDto.username} already exists")
        }

        existingUser.apply {
            username = userDto.username
            email = userDto.email
            firstName = userDto.firstName
            lastName = userDto.lastName
            age = userDto.age
            updatedAt = LocalDateTime.now()
        }

        val updatedUser = userRepository.save(existingUser)

        kafkaProducerService.sendUserEvent(
            UserEvent(
                eventId = UUID.randomUUID().toString(),
                userId = updatedUser.id!!,
                userEmail = updatedUser.email,
                eventType = UserEventType.UPDATED,
                userData = userMapper.toDto(updatedUser)
            )
        )

        return userMapper.toDto(updatedUser)
    }

    @Transactional
    @MonitorPerformance
    override fun deleteUser(id: Long) {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User with id $id not found") }

        userRepository.delete(user)

        kafkaProducerService.sendUserEvent(
            UserEvent(
                eventId = UUID.randomUUID().toString(),
                userId = id,
                userEmail = user.email,
                eventType = UserEventType.DELETED,
                userData = null
            )
        )
    }

    @Transactional(readOnly = true)
    override fun searchUsersByName(name: String): List<UserDto> {
        return userRepository.searchByName(name)
            .map { userMapper.toDto(it) }
    }

    @Transactional(readOnly = true)
    override fun getUsersByMinAge(minAge: Int): List<UserDto> {
        return userRepository.findUsersByMinAge(minAge)
            .map { userMapper.toDto(it) }
    }
}