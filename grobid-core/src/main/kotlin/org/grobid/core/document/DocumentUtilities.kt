package org.grobid.core.document

import org.grobid.core.layout.Block
import org.grobid.core.layout.BoundingBox
import org.grobid.core.layout.LayoutToken
import java.util.stream.Collectors

object DocumentUtilities {

    private fun calculateMedian(numbers: List<Double>): Double {
        if (numbers.isEmpty()) {
            throw IllegalArgumentException("The list cannot be empty")
        }

        val sortedNumbers = numbers.sorted()
        val size = sortedNumbers.size

        return if (size % 2 == 1) {
            sortedNumbers[size / 2]
        } else {
            val middle1 = sortedNumbers[size / 2 - 1]
            val middle2 = sortedNumbers[size / 2]
            (middle1 + middle2) / 2
        }
    }

    fun getMedianBlockVerticalDistance(document: Document): Double {
        // Median distance between blocks
        var previousBlock: Block? = null
        val distances: MutableList<Double> = ArrayList()
        val filteredBlocks = document.getBlocks().stream()
            .filter { b: Block -> b.boundingBox != null }
            .sorted { b1: Block, b2: Block ->
                if (b1.page.number != b2.page.number) {
                    b1.page.number.compareTo(b2.page.number)
                } else {
                    (b1.boundingBox!!.y).compareTo(b2.boundingBox!!.y)
                }
            }
            .collect(Collectors.toList())

        for (block in filteredBlocks) {
            if (
                previousBlock == null
                || previousBlock.page.number != block.page.number
            ) {
                previousBlock = block
                continue
            }

            val distance = previousBlock.boundingBox.verticalDistanceTo(block.boundingBox)
            if (distance > 0) {
                distances.add(distance)
            }
            previousBlock = block
        }

        return calculateMedian(distances)
    }

    fun getMedianLineVerticalDistance(
        document: Document
    ): Double {

        val filteredTokensSortedVertically = document.getBlocks().stream()
            .filter { b: Block -> b.boundingBox != null }
            .flatMap { b: Block -> b.tokens.stream() }
            .collect(Collectors.groupingBy { l: LayoutToken -> Pair(l.page, l.y) })
            .values
            .stream()
            .map { tokens -> tokens[0] }
            .filter { l: LayoutToken -> l.x >= 0 && l.y >= 0 }
            .sorted { l1: LayoutToken, l2: LayoutToken ->
                if (l1.page != l2.page) {
                    l1.page.compareTo(l2.page)
                } else {
                    l1.y.compareTo(l2.y)
                }
            }
            .collect(Collectors.toList())

        var previousToken: LayoutToken? = null
        val distances: MutableList<Double> = ArrayList()
        for (token in filteredTokensSortedVertically) {
            if (
                previousToken == null
                || previousToken.page != token.page
            ) {
                previousToken = token
                continue
            }

            val distance = BoundingBox
                .fromLayoutToken(previousToken)
                .verticalDistanceTo(BoundingBox.fromLayoutToken(token))

            if (distance > 0) {
                distances.add(distance)
            }
            previousToken = token
        }

        return calculateMedian(distances)
    }
}