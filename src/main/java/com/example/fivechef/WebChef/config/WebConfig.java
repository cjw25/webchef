package com.example.fivechef.WebChef.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.course-upload-dir:uploads/course}")
    private String courseUploadDir;

    @Value("${file.inquiry-upload-dir:uploads/inquiry}")
    private String inquiryUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path coursePath = Paths.get(courseUploadDir)
                .toAbsolutePath()
                .normalize();

        Path inquiryPath = Paths.get(inquiryUploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/course/**")
                .addResourceLocations(coursePath.toUri().toString());

        registry.addResourceHandler("/uploads/inquiry/**")
                .addResourceLocations(inquiryPath.toUri().toString());
    }
}