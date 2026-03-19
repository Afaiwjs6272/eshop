package ru.ukhanov.shop.aspect

import io.micrometer.core.instrument.MeterRegistry
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Aspect
@Component
class PerformanceAspect(
    private val meterRegistry: MeterRegistry
) {
    private val methodCallCounts = ConcurrentHashMap<String, AtomicLong>()
    private val methodExecutionTimes = ConcurrentHashMap<String, AtomicLong>()

    @Pointcut("@annotation(com.example.demo.aspect.MonitorPerformance)")
    fun monitoredMethods() {}

    @Around("monitoredMethods()")
    fun monitorPerformance(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = "${joinPoint.target.javaClass.simpleName}.${joinPoint.signature.name}"

        val startTime = System.nanoTime()
        val startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        return try {
            val result = joinPoint.proceed()

            val duration = System.nanoTime() - startTime
            val endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val memoryUsed = endMemory - startMemory

            methodCallCounts.computeIfAbsent(methodName) { AtomicLong() }.incrementAndGet()
            methodExecutionTimes.computeIfAbsent(methodName) { AtomicLong() }.addAndGet(duration)

            meterRegistry.timer("method.execution.time", "method", methodName)
                .record((duration.toDouble() / 1_000_000.0).toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)

            meterRegistry.counter("method.calls", "method", methodName).increment()

            if (memoryUsed > 0) {
                meterRegistry.summary("method.memory.used", "method", methodName)
                    .record(memoryUsed.toDouble())
            }

            result
        } catch (e: Throwable) {
            meterRegistry.counter("method.errors", "method", methodName, "error", e.javaClass.simpleName)
                .increment()
            throw e
        }
    }
}
