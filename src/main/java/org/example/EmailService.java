package org.example;

@Component
public class EmailService implements Service{
    @Override
    public void executeService() {
        System.out.println("sending email...");
    }
}
