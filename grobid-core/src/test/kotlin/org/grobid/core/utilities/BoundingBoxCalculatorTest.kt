package org.grobid.core.utilities

import org.grobid.core.layout.BoundingBox
import org.grobid.core.layout.GraphicObject
import org.grobid.core.layout.GraphicObjectType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BoundingBoxCalculatorTest {
    @Test
    fun testMergeBoundingBoxesIfTouching_empty() {
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_noTouching() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 20.0, 20.0, 10.0, 10.0)
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2))
        assertEquals(2, result.size)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_touching() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 10.0, 0.0, 10.0, 10.0) // Touches box1 on the right
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2))
        assertEquals(1, result.size)
        val merged = result[0]!!.boundingBox
        assertEquals(0.0, merged.x, 0.01)
        assertEquals(0.0, merged.y, 0.01)
        assertEquals(20.0, merged.width, 0.01)
        assertEquals(10.0, merged.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_contained() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 20.0, 20.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 5.0, 5.0, 5.0, 5.0) // Contained in box1
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2))
        assertEquals(1, result.size)
        val merged = result[0]!!.boundingBox
        assertEquals(box1, merged)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_mergeX() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 10.0, 0.0, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 20.0, 0.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(1, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(30.0, merged1.width, 0.01)
        assertEquals(10.0, merged1.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_mergeX2() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 8.0, 0.0, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 15.0, 0.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(1, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(25.0, merged1.width, 0.01)
        assertEquals(10.0, merged1.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_mergeY() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 0.0, 10.0, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 0.0, 20.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(1, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(10.0, merged1.width, 0.01)
        assertEquals(30.0, merged1.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_largeConflict_shouldMergeThroughX() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 10.0, 0.0, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 0.0, 10.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(2, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(20.0, merged1.width, 0.01)
        assertEquals(10.0, merged1.height, 0.01)

        val merged2 = result[1]!!.boundingBox
        assertEquals(0.0, merged2.x, 0.01)
        assertEquals(10.0, merged2.y, 0.01)
        assertEquals(10.0, merged2.width, 0.01)
        assertEquals(10.0, merged2.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_largeConflict_shouldMergeThroughX_2() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 10.0, 0.0, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 1.0, 11.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(2, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(20.0, merged1.width, 0.01)
        assertEquals(10.0, merged1.height, 0.01)

        val merged2 = result[1]!!.boundingBox
        assertEquals(1.0, merged2.x, 0.01)
        assertEquals(11.0, merged2.y, 0.01)
        assertEquals(10.0, merged2.width, 0.01)
        assertEquals(10.0, merged2.height, 0.01)
    }

    @Test
    fun testMergeBoundingBoxesIfTouching_largeConflict_shouldMergeThroughX_3() {
        val box1 = BoundingBox.fromPointAndDimensions(1, 0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox.fromPointAndDimensions(1, 10.0, 1.1, 10.0, 10.0) // Touches box1
        val box3 = BoundingBox.fromPointAndDimensions(1, 1.0, 11.0, 10.0, 10.0) // Touches box2
        val go1 = GraphicObject(box1, GraphicObjectType.BITMAP)
        val go2 = GraphicObject(box2, GraphicObjectType.BITMAP)
        val go3 = GraphicObject(box3, GraphicObjectType.BITMAP)
        val result = BoundingBoxCalculator.mergeBoundingBoxesIfTouching(mutableListOf(go1, go2, go3))
        assertEquals(3, result.size)
        val merged1 = result[0]!!.boundingBox
        assertEquals(0.0, merged1.x, 0.01)
        assertEquals(0.0, merged1.y, 0.01)
        assertEquals(10.0, merged1.width, 0.01)
        assertEquals(10.0, merged1.height, 0.01)

        val merged2 = result[1]!!.boundingBox
        assertEquals(10.0, merged2.x, 0.01)
        assertEquals(1.1, merged2.y, 0.01)
        assertEquals(10.0, merged2.width, 0.01)
        assertEquals(10.0, merged2.height, 0.01)
    }
}

