package com.ciobert.wol.domain.usecase

import com.ciobert.wol.data.local.entity.PcEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

class CheckPcStatusUseCase @Inject constructor() {
    suspend operator fun invoke(pc: PcEntity): Boolean = withContext(Dispatchers.IO) {
        val targets = listOfNotNull(pc.internalIp, pc.externalIp).filter { it.isNotBlank() && !it.endsWith(".255") }
        if (targets.isEmpty()) return@withContext false

        val ports = if (pc.statusCheckPort != null) {
            listOf(pc.statusCheckPort)
        } else {
            listOf(80, 3389, 22, 443, 445)
        }

        // Run all checks in parallel and return on first success
        try {
            coroutineScope {
                val deferredResults = targets.flatMap { ip ->
                    ports.map { port ->
                        async {
                            try {
                                Socket().use { socket ->
                                    socket.connect(InetSocketAddress(ip, port), 250) // Fast 250ms timeout
                                    true
                                }
                            } catch (e: Exception) {
                                false
                            }
                        }
                    }
                }
                
                // Wait for any to return true. If one returns true, any{...} will be true.
                deferredResults.any { it.await() }
            }
        } catch (e: Exception) {
            false
        }
    }
}
