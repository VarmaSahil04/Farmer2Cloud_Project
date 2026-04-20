package net.farmtocloud.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI farmToCloudOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Farm-to-Cloud Kitchen API")
                        .description("Direct Farm-to-Cloud Kitchen Supply Chain Platform REST API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BMD Team")
                                .email("team@farmtocloud.net")));
    }
}
