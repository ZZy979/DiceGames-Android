package com.zzy.dicegames.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayUtilsTest {
    @Test
    public void testCreate() {
        assertArrayEquals(new int[] {8, 8, 8, 8, 8}, ArrayUtils.create(5, 8));
        assertArrayEquals(new int[0], ArrayUtils.create(0, 999));
        assertArrayEquals(new boolean[] {false, false, false}, ArrayUtils.create(3, false));
    }

    @Test
    public void testFill() {
        int[] a = {1, 2, 3, 4, 5, 6};
        assertSame(a, ArrayUtils.fill(a, 42));
        assertArrayEquals(new int[] {42, 42, 42, 42, 42, 42}, a);

        boolean[] b = {true, false, false, true, false};
        assertSame(b, ArrayUtils.fill(b, true));
        assertArrayEquals(new boolean[] {true, true, true, true, true}, b);

        assertNull(ArrayUtils.fill(null, 8));
    }

    @Test
    public void testAll() {
        int[] a1 = {6, 6, 6, 6, 6}, a2 = {6, 6, 6, 5, 6}, a3 = {};
        assertTrue(ArrayUtils.all(a1, 6));
        assertFalse(ArrayUtils.all(a1, 8));
        assertFalse(ArrayUtils.all(a2, 6));
        assertFalse(ArrayUtils.all(a2, 5));
        assertTrue(ArrayUtils.all(a3, 42));
        assertTrue(ArrayUtils.all(a3, 888));
        assertFalse(ArrayUtils.all(null, 0));

        boolean[] b1 = {true, true, true}, b2 = {false, true, false}, b3 = {};
        assertTrue(ArrayUtils.all(b1, true));
        assertFalse(ArrayUtils.all(b1, false));
        assertFalse(ArrayUtils.all(b2, true));
        assertFalse(ArrayUtils.all(b2, false));
        assertTrue(ArrayUtils.all(b3, true));
        assertTrue(ArrayUtils.all(b3, false));
        assertFalse(ArrayUtils.all(null, false));
    }

    @Test
    public void testJoin() {
        int[] a1 = {1, 2, 3, 4, 5}, a2 = {42}, a3 = {};
        assertEquals("1,2,3,4,5", ArrayUtils.join(a1, ","));
        assertEquals("12345", ArrayUtils.join(a1, ""));
        assertEquals("42", ArrayUtils.join(a2, "-"));
        assertEquals("", ArrayUtils.join(a3, ","));
        assertEquals("", ArrayUtils.join(null, ","));
    }
}
