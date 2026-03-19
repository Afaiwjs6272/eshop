package ru.ukhanov.shop.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.ukhanov.shop.model.User
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    fun findByUsername(username: String): Optional<User>

    @Query("SELECT u FROM User u WHERE u.age >= :minAge")
    fun findUsersByMinAge(@Param("minAge") minAge: Int): List<User>

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(@Param("name") name: String): List<User>

    fun existsByEmail(email: String): Boolean

    fun existsByUsername(username: String): Boolean

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    fun countByRole(@Param("role") role: String): Long

    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<User>
}