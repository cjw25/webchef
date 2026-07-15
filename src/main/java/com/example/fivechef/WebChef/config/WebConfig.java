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


    @Value("${file.notice-upload-dir:uploads/notice}")
    private String noticeUploadDir;

    @Value("${file.tips-upload-dir:uploads/tips}")
    private String tipsUploadDir;

    @Value("${file.community-upload-dir:uploads/community}")
    private String communityUploadDir;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path coursePath = Paths.get(courseUploadDir)
                .toAbsolutePath()
                .normalize();

        Path inquiryPath = Paths.get(inquiryUploadDir)
                .toAbsolutePath()
                .normalize();


        Path noticePath = Paths.get(noticeUploadDir)
                .toAbsolutePath()
                .normalize();

        Path tipsPath = Paths.get(tipsUploadDir)
                .toAbsolutePath()
                .normalize();

        Path communityPath = Paths.get(communityUploadDir)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/course/**")
                .addResourceLocations(coursePath.toUri().toString());

        registry.addResourceHandler("/uploads/inquiry/**")
                .addResourceLocations(inquiryPath.toUri().toString());


        registry.addResourceHandler("/uploads/notice/**")
                .addResourceLocations(noticePath.toUri().toString());

        registry.addResourceHandler("/uploads/tips/**")
                .addResourceLocations(tipsPath.toUri().toString());

        registry.addResourceHandler("/uploads/community/**")
                .addResourceLocations(communityPath.toUri().toString());

    }
}