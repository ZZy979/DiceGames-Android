package com.zzy.dicegames.utils;

import java.util.Arrays;
import java.util.function.IntPredicate;

public class ArrayUtil {

    /** 创建长度为length、所有元素都为value的数组 */
    public static int[] create(int length, int value) {
        int[] a = new int[length];
        Arrays.fill(a, value);
        return a;
    }

    /** 创建长度为length、所有元素都为value的数组 */
    public static boolean[] create(int length, boolean value) {
        boolean[] a = new boolean[length];
        Arrays.fill(a, value);
        return a;
    }

    /** 将数组的所有元素都设置为value，并返回原数组 */
    public static int[] fill(int[] a, int value) {
        if (a != null)
            Arrays.fill(a, value);
        return a;
    }

    /** 将数组的所有元素都设置为value，并返回原数组 */
    public static boolean[] fill(boolean[] a, boolean value) {
        if (a != null)
            Arrays.fill(a, value);
        return a;
    }

    /** 判断数组的所有元素是否都等于value */
    public static boolean all(int[] a, int value) {
        if (a == null) return false;
        for (int elem : a)
            if (elem != value)
                return false;
        return true;
    }

    /** 判断数组的所有元素是否都等于value */
    public static boolean all(boolean[] a, boolean value) {
        if (a == null) return false;
        for (boolean elem : a)
            if (elem != value)
                return false;
        return true;
    }

    /** 返回数组中等于给定值的元素个数 */
    public static int count(int[] a, int value) {
        if (a == null) return 0;
        int res = 0;
        for (int elem : a)
            if (elem == value)
                res++;
        return res;
    }

    /** 返回数组中等于给定值的元素个数 */
    public static int count(boolean[] a, boolean value) {
        if (a == null) return 0;
        int res = 0;
        for (boolean elem : a)
            if (elem == value)
                res++;
        return res;
    }

    /** 返回数组中满足给定条件的元素个数 */
    public static int count(int[] a, IntPredicate p) {
        if (a == null) return 0;
        int res = 0;
        for (int elem : a)
            if (p.test(elem))
                res++;
        return res;
    }

    /** 返回数组中所有元素之和 */
    public static int sum(int[] a) {
        return sum(a, 0, a.length);
    }

    /** 返回子数组a[start:end]的所有元素之和（不包括end） */
    public static int sum(int[] a, int start, int end) {
        int res = 0;
        for (int i = start; i < end; i++)
            res += a[i];
        return res;
    }

    /** 返回满足给定条件的元素构成的数组 */
    public static int[] filter(int[] a, IntPredicate p) {
        if (a == null) return null;
        int[] res = new int[count(a, p)];
        int n = 0;
        for (int elem : a)
            if (p.test(elem))
                res[n++] = elem;
        return Arrays.copyOf(res, n);
    }

    /** 使用指定的分隔符连接所有元素 */
    public static String join(int[] a, String delimiter) {
        if (a == null || a.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(Integer.toString(a[0]));
        for (int i = 1; i < a.length; i++)
            sb.append(delimiter).append(a[i]);
        return sb.toString();
    }
}
