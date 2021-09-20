package com.atguigu.gulimall.search.test;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author zero
 * @create 2020-09-30 7:32
 */
public class T3 {
    public static void main(String[] args) {
//        for (int i = 0; i < 3; i++) {
//            for (int m = 3-1; m >i; m--) {
//                System.out.print(" ");
//            }
//            for (int n = 1; n <= 2*i+1; n++) {
//                System.out.print("*");
//            }
//            System.out.println();
//        }
//        print1(3);

        ArrayList<String> list = new ArrayList<String>();
        list.add("hello");
        list.add("java");
        list.add("world");

//        for (int i = 0; i < list.size(); i++) {
//            if(list.get(i).equalsIgnoreCase("java")){
//                list.remove(i);
//            }
//            System.out.println(list.get(i));
//        }
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if ("java".equals(next)) {
//                iterator.remove();
                list.remove(next);
                continue;
            }
            System.out.println(next);
        }






    }

    private static void print1(int cen){
        for (int i = 1; i <= cen; i++) {
            for (int m = cen-1; m >=i ; m--) {
                System.out.print(" ");
            }
            for (int m = 1; m <= 2*i - 1; m++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }

}
