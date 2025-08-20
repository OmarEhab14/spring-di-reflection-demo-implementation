package org.example;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class ApplicationContext {
    private final Map<Class<?>, Object> instanceMap = new HashMap<>();
    private final Map<String, Object> idToInstanceMap = new HashMap<>();

    public ApplicationContext(String packageName) {
        List<Class<?>> candidates = scanPackage(packageName);
        try {
            // first, we instantiate the @Component classes
            for (Class<?> klass : candidates) {
                if (klass.isAnnotationPresent(Component.class)) {
                    Object instance = klass.getDeclaredConstructor().newInstance();
                    instanceMap.put(klass, instance);
                    if (!klass.getAnnotation(Component.class).value().isBlank()) {
                        idToInstanceMap.put(klass.getAnnotation(Component.class).value(), instance);
                    }
                }
            }

            // second, we inject the @Autowired fields of the classes with the appropriate dependencies
            for (Object instance : instanceMap.values()) {
                for (Field field : instance.getClass().getDeclaredFields()) {
                    Object dependency = null;
                    if (field.isAnnotationPresent(Autowired.class)) {
                        if (field.isAnnotationPresent(Qualifier.class)) {
                            dependency = idToInstanceMap.get(field.getAnnotation(Qualifier.class).value());
                        } else {
                            dependency = instanceMap.get(field.getType()); // should be null in case if I autowired an interface and not a concrete class
                        }
                        if (dependency == null) {
                            if (field.getType().isInterface()) {
                                boolean flag = false;
                                for (Object obj : instanceMap.values()) {
                                    if (field.getType().isAssignableFrom(obj.getClass())) {
                                        if (flag) {
                                            throw new IllegalStateException("Duplicate dependency found for field " + field.getName());
                                        }
                                        flag = true;
                                        dependency = obj;
                                    }
                                }
                            }
                        }
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
