package me.realignist.springboot.router.example;

import me.realignist.springboot.router.RouterConfigurationSupport;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "me.realignist.springboot.router.example")
@PropertySource("application.properties")
public class ApplicationConfig extends RouterConfigurationSupport {

    @Override
    public List<String> listRouteFiles () {
        List<String> routeFiles = new ArrayList<String>();
        routeFiles.add("/Users/Realignist/Desktop/springboot-single-router/src/main/resources/routes.conf");
        return routeFiles;
    }
}