package ru.ukhanov.shop.controller


import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.ukhanov.shop.dto.UserDto
import ru.ukhanov.shop.service.user.UserService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(@Valid @RequestBody userDto: UserDto): ResponseEntity<UserDto> {
        val createdUser = userService.createUser(userDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(page, size)
        return ResponseEntity.ok(users)
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody userDto: UserDto
    ): ResponseEntity<UserDto> {
        val updatedUser = userService.updateUser(id, userDto)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam name: String): ResponseEntity<List<UserDto>> {
        val users = userService.searchUsersByName(name)
        return ResponseEntity.ok(users)
    }

    @GetMapping("/by-age")
    fun getUsersByMinAge(@RequestParam minAge: Int): ResponseEntity<List<UserDto>> {
        val users = userService.getUsersByMinAge(minAge)
        return ResponseEntity.ok(users)
    }
}