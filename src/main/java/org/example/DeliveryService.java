package org.example;

@Component("deliveryService")
public class DeliveryService implements Service {
    @Override
    public void executeService() {
        System.out.println("delivery service");
    }
}
