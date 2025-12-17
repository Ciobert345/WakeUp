package com.gemini.wol.domain.usecase

import com.gemini.wol.data.repository.PcRepository
import com.gemini.wol.domain.MagicPacketService
import javax.inject.Inject

class SendWakePacketUseCase @Inject constructor(
    private val magicPacketService: MagicPacketService,
    private val pcRepository: PcRepository
) {
    suspend operator fun invoke(pcId: String): Result<Unit> {
        return try {
            val pc = pcRepository.getPcById(pcId) ?: throw IllegalArgumentException("PC not found")
            val broadcastIp = pc.broadcastIp ?: "255.255.255.255" // Default to global broadcast if null
            magicPacketService.sendMagicPacket(pc.mac, broadcastIp, pc.port)
            
            // Log success or update last triggered time if we had that field
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
