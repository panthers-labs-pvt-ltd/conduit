package org.pantherslabs.chimera.conduit.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.pantherslabs.chimera.conduit.api")
@MapperScan("org.pantherslabs.chimera.conduit.api.mapper")
@MapperScan("org.pantherslabs.chimera.unisca.api_nexus.api_nexus_client.dynamic_query.mapper")
public class ConduitAPI {
    public static void main(String[] args) {
        SpringApplication.run(ConduitAPI.class, args);
    }
}
