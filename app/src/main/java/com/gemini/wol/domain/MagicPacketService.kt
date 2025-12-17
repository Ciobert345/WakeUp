package com.gemini.wol.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

class MagicPacketService @Inject constructor() {

    suspend fun sendMagicPacket(macStr: String, broadcastIp: String, port: Int = 9) = withContext(Dispatchers.IO) {
        try {
            val macBytes = macStr.split("[:\\-]".toRegex())
                .map { it.toInt(16).toByte() }
                .toByteArray()
            
            if (macBytes.size != 6) throw IllegalArgumentException("Invalid MAC address")

            val packet = ByteArray(6 + 16 * 6)
            for (i in 0 until 6) packet[i] = 0xFF.toByte()
            for (i in 6 until packet.size) packet[i] = macBytes[(i - 6) % 6]

            val socket = DatagramSocket()
            socket.broadcast = true
            val address = InetAddress.getByName(broadcastIp)
            val datagram = DatagramPacket(packet, packet.size, address, port)
            socket.send(datagram)
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
