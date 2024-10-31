package com.sonsofcrypto.web3lib.extensions

import kotlin.math.min

/** Returns sublist to index. If index is greater than list returns full list */
inline fun <E>List<out E>.subListTo(index: Int): List<E> =
    if (isEmpty()) this
    else this.subList(0, min(index, this.size))