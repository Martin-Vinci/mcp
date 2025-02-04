package com.greybox.mediums.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class CorsConfig {
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("*")
//                        .allowedMethods("PUT", "DELETE", "GET", "POST")
//                        .allowedHeaders("*")
//                        .exposedHeaders("*")
//                        .allowCredentials(false).maxAge(3600);
//            }
//        };
//    }
}
