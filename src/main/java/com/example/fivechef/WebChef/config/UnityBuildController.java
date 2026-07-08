package com.example.fivechef.WebChef.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

@Controller
public class UnityBuildController {

    @GetMapping("/unity/Build/{fileName:.+}")
    public ResponseEntity<Resource> getUnityBuildFile(
            @PathVariable String fileName
    ) {
        Resource resource = new ClassPathResource("static/unity/Build/" + fileName);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();

        if (fileName.endsWith(".br")) {
            headers.add(HttpHeaders.CONTENT_ENCODING, "br");
        }

        if (fileName.endsWith(".wasm.br") || fileName.endsWith(".wasm")) {
            headers.setContentType(MediaType.parseMediaType("application/wasm"));
        } else if (fileName.endsWith(".js.br") || fileName.endsWith(".js")) {
            headers.setContentType(MediaType.parseMediaType("application/javascript"));
        } else if (fileName.endsWith(".data.br") || fileName.endsWith(".data")) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().cachePrivate())
                .body(resource);
    }
}