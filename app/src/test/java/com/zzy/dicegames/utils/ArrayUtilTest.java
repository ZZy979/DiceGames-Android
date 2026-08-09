package com.zzy.dicegames.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayUtilTest {
    @Test
    public void testCreate() {
        assertArrayEquals(new int[] {8, 8, 8, 8, 8}, ArrayUtil.create(5, 8));
        assertArrayEquals(new int[0], ArrayUtil.create(0, 999));
        assertArrayEquals(new boolean[] {false, false, false}, ArrayUtil.create(3, false));
    }

    @Test
    public void testFill() {
        int[] a = {1, 2, 3, 4, 5, 6};
        assertSame(a, ArrayUtil.fill(a, 42));
        assertArrayEquals(new int[] {42, 42, 42, 42, 42, 42}, a);

        boolean[] b = {true, false, false, true, false};
        assertSame(b, ArrayUtil.fill(b, true));
        assertArrayEquals(new boolean[] {true, true, true, true, true}, b);

        assertNull(ArrayUtil.fill(null, 8));
    }

    @Test
    public void testAll() {
        int[] a1 = {6, 6, 6, 6, 6}, a2 = {6, 6, 6, 5, 6}, a3 = {};
        assertTrue(ArrayUtil.all(a1, 6));
        assertFalse(ArrayUtil.all(a1, 8));
        assertFalse(ArrayUtil.all(a2, 6));
        assertFalse(ArrayUtil.all(a2, 5));
        assertTrue(ArrayUtil.all(a3, 42));
        assertTrue(ArrayUtil.all(a3, 888));
        assertFalse(ArrayUtil.all(null, 0));

        boolean[] b1 = {true, true, true}, b2 = {false, true, false}, b3 = {};
        assertTrue(ArrayUtil.all(b1, true));
        assertFalse(ArrayUtil.all(b1, false));
        assertFalse(ArrayUtil.all(b2, true));
        assertFalse(ArrayUtil.all(b2, false));
        assertTrue(ArrayUtil.all(b3, true));
        assertTrue(ArrayUtil.all(b3, false));
        assertFalse(ArrayUtil.all(null, false));
    }

    @Test
    public void testCount() {
        int[] a1 = {8, 6, 5, 8, 8, 7, 8}, a2 = {42}, a3 = {};
        assertEquals(4, ArrayUtil.count(a1, 8));
        assertEquals(0, ArrayUtil.count(a1, 888));
        assertEquals(1, ArrayUtil.count(a2, 42));
        assertEquals(0, ArrayUtil.count(a3, 6));
        assertEquals(0, ArrayUtil.count(null, 1));

        boolean[] b1 = {true, false, false}, b2 = {true}, b3 = {};
        assertEquals(1, ArrayUtil.count(b1, true));
        assertEquals(2, ArrayUtil.count(b1, false));
        assertEquals(1, ArrayUtil.count(b2, true));
        assertEquals(0, ArrayUtil.count(b2, false));
        assertEquals(0, ArrayUtil.count(b3, true));
        assertEquals(0, ArrayUtil.count(b3, false));
        assertEquals(0, ArrayUtil.count(null, true));
    }

    @Test
    public void testCountPredicate() {
        int[] a1 = {8, 6, 5, 8, 8, 7, 8}, a2 = {42}, a3 = {};
        assertEquals(3, ArrayUtil.count(a1, x -> x < 8));
        assertEquals(0, ArrayUtil.count(a1, x -> x > 10));
        assertEquals(2, ArrayUtil.count(a1, (i, x) -> i < 4 && x == 8));
        assertEquals(1, ArrayUtil.count(a2, x -> x % 2 == 0));
        assertEquals(0, ArrayUtil.count(a3, x -> x != 0));
        assertEquals(0, ArrayUtil.count(null, x -> x != 0));
    }

    @Test
    public void testSum() {
        int[] a1 = {1, 2, 3, 4, 5}, a2 = {42}, a3 = {};
        assertEquals(15, ArrayUtil.sum(a1));
        assertEquals(42, ArrayUtil.sum(a2));
        assertEquals(0, ArrayUtil.sum(a3));
        assertEquals(10, ArrayUtil.sum(a1, 0, 4));
        assertEquals(5, ArrayUtil.sum(a1, 1, 3));
        assertEquals(0, ArrayUtil.sum(a1, 2, 2));
    }

    @Test
    public void testFilter() {
        int[] a1 = {1, 2, 3, 4, 5, 6, 7, 8}, a2 = {888}, a3 = {};
        assertArrayEquals(new int[] {2, 4, 6, 8}, ArrayUtil.filter(a1, x -> x % 2 == 0));
        assertArrayEquals(new int[0], ArrayUtil.filter(a1, x -> x <= 0));
        assertArrayEquals(new int[] {888}, ArrayUtil.filter(a2, x -> x > 100));
        assertArrayEquals(new int[0], ArrayUtil.filter(a3, x -> x != 0));
        assertNull(ArrayUtil.filter(null, x -> x != 0));
    }

    @Test
    public void testJoin() {
        int[] a1 = {1, 2, 3, 4, 5}, a2 = {42}, a3 = {};
        assertEquals("1,2,3,4,5", ArrayUtil.join(a1, ","));
        assertEquals("12345", ArrayUtil.join(a1, ""));
        assertEquals("42", ArrayUtil.join(a2, "-"));
        assertEquals("", ArrayUtil.join(a3, ","));
        assertEquals("", ArrayUtil.join(null, ","));
    }
}
