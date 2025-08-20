package org.example;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ApplicationContext {
    private final Map<Class<?>, Object> instanceMap = new HashMap<>();

    public ApplicationContext(String packageName) {
        List<Class<?>> candidates = scanPackage(packageName);
        try {
            // first, we instantiate the @Component classes
            for (Class<?> klass : candidates) {
                if (klass.isAnnotationPresent(Component.class)) {
                    Object instance = klass.getDeclaredConstructor().newInstance();
                    instanceMap.put(klass, instance);
                }
            }

            // second, we inject the @Autowired fields of the classes with the appropriate dependencies
            for (Object instance : instanceMap.values()) {
                for (Field field : instance.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Object dependency = instanceMap.get(field.getType());
                        field.setAccessible(true);
                        field.set(instance, dependency);
                    }
                }
            }
        }  catch (Exception e) {
            System.out.println("failed to initialize the application context: " + e.getMessage());
        }
    }

    private List<Class<?>> scanPackage(String packageName) {
        List<Class<?>> result = new ArrayList<>();
        String path = packageName.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new RuntimeException("Can't find package: " + path);
        }
        try {
            File directory = new File(url.toURI());
            for (String fileName : Objects.requireNonNull(directory.list())) {
                if (fileName.endsWith(".class")) {
                    String className = packageName + "." + fileName.substring(0, fileName.length() - 6); // com.example.className
                    Class<?> klass = Class.forName(className);
                    result.add(klass);
                }
            }
            return result;
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getBean(Class<T> klass) {
        return (T) instanceMap.get(klass);
    }
}
