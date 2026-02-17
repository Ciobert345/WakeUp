package com.ciobert.wol.domain.usecase

import android.util.Log
import com.ciobert.wol.data.repository.PcRepository
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.StringTokenizer
import javax.inject.Inject

/**
 * Unified Nuclear Wake Engine.
 * Monolithic implementation to bypass classpath issues and maximize background reliability.
 */
class SendWakePacketUseCase @Inject constructor(
    private val pcRepository: PcRepository
) {
    companion object {
        private const val TAG = "SendWakePacket"
    }

    suspend operator fun invoke(pcId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pc = pcRepository.getPcById(pcId) ?: throw IllegalArgumentException("PC not found")
            
            // Parallel execution with explicit completion tracking
            val results = supervisorScope {
                val deferreds = mutableListOf<Deferred<Boolean>>()

                // 1. Internal IP (Unicast)
                if (!pc.internalIp.isNullOrBlank()) {
                    deferreds.add(async {
                        sendPacketInternal(pc.mac, pc.internalIp!!, pc.internalPort)
                    })
                }
                
                // 2. Global Broadcast (Safety net)
                deferreds.add(async {
                    sendPacketInternal(pc.mac, "255.255.255.255", pc.internalPort)
                })

                // 3. External IP (WAN)
                if (!pc.externalIp.isNullOrBlank()) {
                    deferreds.add(async {
                        sendPacketInternal(pc.mac, pc.externalIp!!, pc.externalPort)
                    })
                }

                deferreds.awaitAll()
            }

            if (results.any { it }) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("All wake attempts failed or timed out"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical engine failure: " + e.message)
            Result.failure(e)
        }
    }

    /**
     * Low-level Magic Packet dispatch using Java primitives to guarantee
     * compilation even when Kotlin stdlib resolution is spotty.
     */
    private suspend fun sendPacketInternal(mac: String, ip: String, port: Int): Boolean {
        return try {
            withTimeout(15000L) { // 15s for slow cellular DNS
                val st = StringTokenizer(mac, ":-")
                val macBytes = ByteArray(6)
                var count = 0
                while (st.hasMoreTokens() && count < 6) {
                    val token = st.nextToken()
                    val value = Integer.parseInt(token, 16)
                    macBytes[count] = value.toByte()
                    count++
                }

                if (count != 6) return@withTimeout false

                val packetData = ByteArray(102)
                for (i in 0..5) {
                    packetData[i] = 255.toByte()
                }
                for (i in 1..16) {
                    System.arraycopy(macBytes, 0, packetData, i * 6, 6)
                }

                val socket = DatagramSocket()
                try {
                    socket.broadcast = true
                    val address = InetAddress.getByName(ip)
                    val datagram = DatagramPacket(packetData, packetData.size, address, port)
                    socket.send(datagram)
                    Log.d(TAG, "âœ“ Magic Packet dispatched to $ip:$port")
                    true
                } finally {
                    socket.close()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "âœ— Dispatch failed for $ip: " + (e.message ?: "Unknown error"))
            false
        }
    }
}
