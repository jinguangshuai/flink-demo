package com.example.demo.utils;

/**
 * @Auther：jinguangshuai
 * @Data：2023/9/5 - 09 - 05 - 10:31
 * @Description:com.example.demo.utils
 * @version:1.0
 */

public class PoleConvertUtil {

    public static void sort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        process(arr, 0, arr.length - 1);
    }
    public static void process(int[] arr, int L, int R) {
        if (L >= R) {
            return;
        }
        swap(arr, L + (int) (Math.random() * (R - L + 1)), R);
        int[] equalArea = partition(arr, L, R);
        process(arr, L, equalArea[0] - 1);
        process(arr, equalArea[1] + 1, R);
    }
    public static int[] partition(int[] arr, int L, int R) {
        if (L > R) {
            return new int[]{-1, -1};
        }
        if (L == R) {
            return new int[]{L, R};
        }
        int less = L - 1;
        int more = R + 1;
        int index = L;
        int num = arr[R];
        while (index < more) {
            if (arr[index] < num) {
                swap(arr, index++, ++less);
            } else if (arr[index] == num) {
                index++;
            } else {
                swap(arr, index, --more);
            }
        }
        return new int[]{less + 1, more - 1};
    }
    public static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    //杆塔段合并
    public  static String convert(int[] arr, int index) {
        int end = index;
        if (arr.length == index) {
            return "";
        } else {
            for (int i = index; i < arr.length; i++) {
                if (i < arr.length - 1) {
                    if (arr[i] + 1 == arr[i + 1]) {
                        end = i;
                    } else {
                        if (i > index)
                            end++;
                        break;
                    }
                } else {
                    if (end == arr.length - 2) {
                        end = arr.length - 1;
                        break;
                    }
                }
            }
            if (index == end){
                return arr[index] + "#," + convert(arr, end + 1);
            }else{
                return arr[index] + "#-" + arr[end] + "#," + convert(arr, end + 1);
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{12,13,1,2,5,4,6,6,18};
        sort(arr);
        String convert = convert(arr, 0);
        System.out.println(convert.substring(0, convert.length() - 1));
    }

}

