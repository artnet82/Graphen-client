package com.graphene.antivirus.scanner

import com.graphene.antivirus.data.NetworkEvent
import com.graphene.antivirus.data.NetworkTelemetry
import java.util.concurrent.ConcurrentHashMap

object NetworkEventStore {

    private const val MAX_EVENTS = 2000
    private const val RETENTION_MS = 24L * 60 * 60 * 1000 // сутки

    private val events = ArrayDeque<NetworkEvent>()
    private val uniqueDomains = ConcurrentHashMap<String, Long>()
    private var capturedSince: Long = System.currentTimeMillis()

    @Synchronized
    fun add(domain: String, queryType: Int = 1) {
        val now = System.currentTimeMillis()
        val ev = NetworkEvent(domain = domain, timestamp = now, queryType = queryType)
        events.addLast(ev)
        uniqueDomains[domain] = now
        while (events.size > MAX_EVENTS) events.removeFirst()
        uniqueDomains.entries.removeIf { it.value < now - RETENTION_MS }
    }

    @Synchronized
    fun snapshot(): NetworkTelemetry {
        val now = System.currentTimeMillis()
        val fresh = events.filter { it.timestamp > now - RETENTION_MS }
        return NetworkTelemetry(
            dnsEvents = fresh.toList(),
            uniqueDomains = fresh.map { it.domain }.toSet().size,
            capturedSince = capturedSince
        )
    }

    @Synchronized
    fun reset() {
        events.clear()
        uniqueDomains.clear()
        capturedSince = System.currentTimeMillis()
    }
}