
package org.delcom.pam_p5_ifs23051.network.todos.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseStats(
    val stats: ResponseStatsData
)

@Serializable
data class ResponseStatsData(
    val total: Long = 0,
    val done: Long = 0,
    val pending: Long = 0
)
