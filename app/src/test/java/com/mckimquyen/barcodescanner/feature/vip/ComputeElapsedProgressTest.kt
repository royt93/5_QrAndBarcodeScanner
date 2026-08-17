package com.mckimquyen.barcodescanner.feature.vip

import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeElapsedProgressTest {

    private val granted = 1_000L
    private val expires = 11_000L // 10_000L window

    @Test
    fun `progress is 0 at the moment of activation`() {
        assertEquals(0, ActVipManagement.computeElapsedProgress(granted, expires, granted))
    }

    @Test
    fun `progress is 100 at expiry`() {
        assertEquals(100, ActVipManagement.computeElapsedProgress(granted, expires, expires))
    }

    @Test
    fun `progress is 50 at the halfway point`() {
        assertEquals(50, ActVipManagement.computeElapsedProgress(granted, expires, 6_000L))
    }

    @Test
    fun `progress clamps to 100 when now is past expiry`() {
        assertEquals(100, ActVipManagement.computeElapsedProgress(granted, expires, 999_999L))
    }

    @Test
    fun `progress clamps to 0 when now is before granted (clock skew)`() {
        assertEquals(0, ActVipManagement.computeElapsedProgress(granted, expires, 0L))
    }

    @Test
    fun `progress is 100 when expiry is not after granted (zero or negative window)`() {
        // total <= 0 -> render full bar (already-expired / clock-skew fallback), never divide by zero.
        assertEquals(100, ActVipManagement.computeElapsedProgress(granted, granted, granted))
        assertEquals(100, ActVipManagement.computeElapsedProgress(granted, granted - 1, granted))
    }
}
