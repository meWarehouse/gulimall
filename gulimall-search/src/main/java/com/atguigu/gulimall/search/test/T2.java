package com.atguigu.gulimall.search.test;

import java.util.Arrays;
import java.util.List;

/**
 * @author zero
 * @create 2020-09-23 7:14
 */
public class T2 {
}
interface A{
    int x = 0;
}
class B{
    int x = 1;
}
class C extends B implements A{
    public void printX(){
        System.out.println(super.x);
    }
    public static void main(String[] args) {
        new C().printX();
    }
}
class Test {

    public static void main(String[] args) {
//        Base b1 = new Base();
//        Base b2 = new Sub();
        A2 a2 = new A2();
        A1 a1 = new A2();
    }
}
class Base{
    Base(){
        method(100);
    }
    public void method(int i){
        System.out.println("base : " + i);
    }
}
class Sub extends Base{
    Sub(){
        super.method(70);
    }
    public void method(int j){
        System.out.println("sub : " + j);
    }
}

class A1{
    public A1() {
    }
}
class A2 extends A1{
    public A2() {
    }
}

class R{
    public static void main(String[] args) {
//        int test = test(3,5);
//        System.out.println(test);
        Integer[] datas = {1,2,3,4,5};
        List<Integer> list = Arrays.asList(datas);
        list.add(5);
        System.out.println(list.size());

    }

    public static int test(int x, int y){
        int result = x;
        try{
            if(x<0 || y<0){
                return 0;
            }
            result = x + y;
            return result;
        }finally{
            result = x - y;
        }
    }
}
