package org.example;

public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext("org.example");
        Authentication authentication = applicationContext.getBean(Authentication.class);
        authentication.executeAuth();
    }
}