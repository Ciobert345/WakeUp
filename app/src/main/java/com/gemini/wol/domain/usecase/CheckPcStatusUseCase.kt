package com.gemini.wol.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class CheckPcStatusUseCase @Inject constructor() {
    suspend operator fun invoke(ip: String?): Boolean = withContext(Dispatchers.IO) {
        if (ip.isNullOrBlank()) return@withContext false
        
        // If it's a broadcast address, we can't really check it.
        if (ip.endsWith(".255")) return@withContext false

        // Check only 2 common ports with reduced timeout
        val ports = listOf(80, 3389) // Web, RDP
        
        for (port in ports) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 150) // 150ms timeout per port
                socket.close()
                return@withContext true
            } catch (e: Exception) {
                // Ignore and try next port
            }
        }
        
        // Skip ICMP ping to reduce overall time
        return@withContext false
    }
}
