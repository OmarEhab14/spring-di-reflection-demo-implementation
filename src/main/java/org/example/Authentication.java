package org.example;

@Component
public class Authentication {
    @Autowired
    @Qualifier("emailService")
    private Service service; // should depend on interface but leave it like that for now until I implement the @Qualifier annotation

    public void executeAuth() {
        System.out.println("executing auth...");
        service.executeService();
    }
}
