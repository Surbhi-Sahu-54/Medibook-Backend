package com.medibook.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiGatewayRouteConfigTest {

    @Test
    void gatewayKeepsDiscoveryRouteLocatorEnabled() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("application.yml"));

        Properties properties = yaml.getObject();

        assertEquals("true", properties.getProperty("spring.cloud.gateway.discovery.locator.enabled"));
        assertEquals("true", properties.getProperty("spring.cloud.gateway.discovery.locator.lower-case-service-id"));
    }
}
