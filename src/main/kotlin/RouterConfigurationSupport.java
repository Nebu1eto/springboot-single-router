package me.realignist.springboot.router;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

public abstract class RouterConfigurationSupport extends DelegatingWebMvcConfiguration {

    @Bean public RouterHandlerMapping createRouterHandlerMapping () {
        RouterHandlerMapping handlerMapping = new RouterHandlerMapping();
        handlerMapping.setRouteFiles(listRouteFiles());
        handlerMapping.setAutoReloadEnabled(isHandlerMappingReloadEnabled());
        handlerMapping.setInterceptors(getInterceptors());
        handlerMapping.setOrder(0);
        return handlerMapping;
    }

    protected boolean isHandlerMappingReloadEnabled () {
        return false;
    }

    @Bean @Override public RequestMappingHandlerMapping requestMappingHandlerMapping () {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setOrder(3);
        handlerMapping.setInterceptors(getInterceptors());
        handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());
        return handlerMapping;
    }

    /**
     * Return the ordered list of route configuration files to be loaded
     * by the Router at startup.
     */
    public abstract List<String> listRouteFiles();
}