package com.smartlibrary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Single global CORS is managed exclusively by SecurityConfig's CorsConfigurationSource
}
