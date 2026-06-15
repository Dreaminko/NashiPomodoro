package com.dreaminko.nashipomodoro.core.update

internal object VersionComparator {
    fun isNewer(candidate: String, current: String): Boolean =
        compare(candidate, current) > 0

    fun compare(left: String, right: String): Int {
        val leftVersion = ParsedVersion.parse(left)
        val rightVersion = ParsedVersion.parse(right)

        val maxCoreSize = maxOf(leftVersion.core.size, rightVersion.core.size)
        repeat(maxCoreSize) { index ->
            val comparison = (leftVersion.core.getOrNull(index) ?: 0)
                .compareTo(rightVersion.core.getOrNull(index) ?: 0)
            if (comparison != 0) return comparison
        }

        if (leftVersion.preRelease.isEmpty() && rightVersion.preRelease.isNotEmpty()) return 1
        if (leftVersion.preRelease.isNotEmpty() && rightVersion.preRelease.isEmpty()) return -1

        val maxPreReleaseSize = maxOf(
            leftVersion.preRelease.size,
            rightVersion.preRelease.size
        )
        repeat(maxPreReleaseSize) { index ->
            val leftPart = leftVersion.preRelease.getOrNull(index) ?: return -1
            val rightPart = rightVersion.preRelease.getOrNull(index) ?: return 1
            val comparison = comparePreReleasePart(leftPart, rightPart)
            if (comparison != 0) return comparison
        }
        return 0
    }

    private fun comparePreReleasePart(left: String, right: String): Int {
        val leftNumber = left.toLongOrNull()
        val rightNumber = right.toLongOrNull()
        return when {
            leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
            leftNumber != null -> -1
            rightNumber != null -> 1
            else -> left.compareTo(right, ignoreCase = true)
        }
    }

    private data class ParsedVersion(
        val core: List<Int>,
        val preRelease: List<String>
    ) {
        companion object {
            fun parse(value: String): ParsedVersion {
                val normalized = value.trim().removePrefix("v").removePrefix("V")
                val withoutBuildMetadata = normalized.substringBefore('+')
                val coreText = withoutBuildMetadata.substringBefore('-')
                val preReleaseText = withoutBuildMetadata.substringAfter('-', "")
                return ParsedVersion(
                    core = coreText.split('.').map { part ->
                        part.takeWhile(Char::isDigit).toIntOrNull() ?: 0
                    },
                    preRelease = preReleaseText
                        .split('.')
                        .filter(String::isNotBlank)
                )
            }
        }
    }
}
