package com.dreaminko.nashipomodoro.core.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun detectsNewerCoreVersion() {
        assertTrue(VersionComparator.isNewer("v1.2.0", "1.1.9"))
        assertTrue(VersionComparator.isNewer("2.0.0", "1.99.99"))
    }

    @Test
    fun treatsMissingCorePartsAsZero() {
        assertFalse(VersionComparator.isNewer("1.0", "1.0.0"))
        assertTrue(VersionComparator.isNewer("1.0.1", "1.0"))
    }

    @Test
    fun comparesPreReleaseVersionsUsingSemanticVersionRules() {
        assertTrue(VersionComparator.isNewer("1.0.0", "1.0.0-beta.2"))
        assertTrue(VersionComparator.isNewer("1.0.0-beta.10", "1.0.0-beta.2"))
        assertFalse(VersionComparator.isNewer("1.0.0-beta.1", "1.0.0"))
    }
}
