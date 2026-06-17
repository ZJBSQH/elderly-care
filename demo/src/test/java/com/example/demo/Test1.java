
package com.example.demo;

import org.testng.annotations.Test;

public class Test1 {
    @Test(priority = 1)
    public void First() {
        System.out.println("1");
    }
    @Test(priority = 2)
    public void Second() {
        System.out.println("2");
    }
    @Test(priority = 3)
    public void Third() {
        System.out.println("3");
    }
}
