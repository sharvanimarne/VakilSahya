package com.vakilsahay.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 3.0 configuration.
 * Access at: http://localhost:8080/api/swagger-ui.html
 * © 2025 VakilSahay
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI vakilSahayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VakilSahay API")
                        .description("""
                            ## Plain-Language Legal Document Explainer API
                            
                            VakilSahay analyzes legal documents (rental agreements, employment contracts, 
                            loan agreements) and translates complex clauses into plain English with 
                            risk severity scores.
                            
                            **PROPRIETARY**: The clause severity scoring algorithm and Indian Legal Clause 
                            Taxonomy are protected under Indian Copyright Act 1957.
                            
                            © 2025 VakilSahay. All rights reserved.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("VakilSahay Team")
                                .email("api@vakilsahay.in"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://vakilsahay.in/terms")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}