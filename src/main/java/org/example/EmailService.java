package org.example;

@Component("emailService")
public class EmailService implements Service{
    @Override
    public void executeService() {
        System.out.println("sending email...");
    }
}
