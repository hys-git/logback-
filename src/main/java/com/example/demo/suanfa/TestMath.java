package com.example.demo.suanfa;

/**
 * @author: faker
 * @DATE: 2020/7/15 15:33
 */
public class TestMath {
    /**
     * @Description
     * 现在有一个自定义数组：
     * int[] a = {1,2,3,4,5,6};
     * 有一个自定义变量：
     * int b = 10;
     * 现在要求是：
     * 在数组a中求出能等于b 的组合
     * @Date 2020/7/15 15:37
     * @Param [args]
     * @return void
     **/
    public static void main(String[] args) {
        int[] a = {1,2,3,4,5,6};
        int b = 10;
        for (int i=0;i<1<<a.length;i++){
            int sum = 0;
            String s = "";
            for (int j=0;j<a.length;j++){
                if ((i>>j&1)==1){
                    sum += a[j];
                    s += a[j]+" ";
                }
            }
            if (sum==b){
                System.out.println(s);
            }
        }
    }
}
