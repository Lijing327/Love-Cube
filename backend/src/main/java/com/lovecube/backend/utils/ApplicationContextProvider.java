package com.lovecube.backend.utils;


import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class ApplicationContextProvider {
    private static ApplicationContext context;

    public ApplicationContextProvider(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}