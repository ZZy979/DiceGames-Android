package com.zzy.dicegames.utils;

import java.util.Arrays;

public class ArrayUtils {

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

    /** 使用指定的分隔符连接所有元素 */
    public static String join(int[] a, String delimiter) {
        if (a == null || a.length == 0)
            return "";
        StringBuilder sb = new StringBuilder(Integer.toString(a[0]));
        for (int i = 1; i < a.length; i++)
            sb.append(delimiter).append(Integer.toString(a[i]));
        return sb.toString();
    }
}
