package com.sonsofcrypto.web3lib_services.GoinGecko.model

import kotlinx.datetime.Instant

data class Candle(
    val timestamp: Instant,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)