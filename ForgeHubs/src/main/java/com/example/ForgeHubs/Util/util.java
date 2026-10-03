package com.example.ForgeHubs.Util;


public class util {

    public static<T> void testData(T ...data){
        System.out.println("count of data: "+ data.length);
        for(T t:data){
            System.out.println(data);
        }
    }
}
