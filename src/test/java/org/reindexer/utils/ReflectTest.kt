package org.reindexer.utils

import org.junit.Assert
import org.junit.Test
import java.util.*

class ReflectTest {

    @Test
    fun testSplitReindexAnnotationValue() {
        val value = "id,,pk"
        Assert.assertEquals(Arrays.asList("id","","pk"), Reflect.splitOptions(value))
    }

    @Test
    fun testFieldType() {
        Assert.assertEquals("int", Reflect.getFieldType(Int::class.java))
        Assert.assertEquals("int", Reflect.getFieldType(Integer::class.java))
        Assert.assertEquals("string", Reflect.getFieldType(String::class.java))
    }
}