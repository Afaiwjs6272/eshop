package ru.ukhanov.shop.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.ukhanov.shop.model.Order
import ru.ukhanov.shop.model.enums.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findByOrderNumber(orderNumber: String): Order?

    fun findByUserId(userId: Long): List<Order>

    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    fun findUserOrdersWithPagination(@Param("userId") userId: Long, pageable: Pageable): Page<Order>

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :date")
    fun findOldOrdersByStatus(@Param("status") status: OrderStatus, @Param("date") date: LocalDateTime): List<Order>

    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    fun getTotalSpentByUser(@Param("userId") userId: Long): BigDecimal?

    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.id IN :orderIds")
    fun bulkUpdateOrderStatus(@Param("orderIds") orderIds: List<Long>, @Param("newStatus") newStatus: OrderStatus): Int

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    fun getOrderStatusCounts(): List<Array<Any>>
}