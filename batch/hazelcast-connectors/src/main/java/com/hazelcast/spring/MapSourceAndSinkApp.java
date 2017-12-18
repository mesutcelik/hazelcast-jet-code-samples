package com.hazelcast.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MapSourceAndSinkApp {

    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "log4j");
        ApplicationContext ctx = new AnnotationConfigApplicationContext(MapSourceAndSinkConfiguration.class);
        MapSourceAndSinkSpring springApp = (MapSourceAndSinkSpring)ctx.getBean(MapSourceAndSinkSpring.class);
        springApp.runApp();


    }
}
