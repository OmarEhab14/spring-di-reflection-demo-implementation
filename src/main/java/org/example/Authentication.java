package org.example;

@Component
public class Authentication {
    @Autowired
    @Qualifier("emailService")
    private Service service;

    public void executeAuth() {
        System.out.println("executing auth...");
        service.executeService();
    }
}
