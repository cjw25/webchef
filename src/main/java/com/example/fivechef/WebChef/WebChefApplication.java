package com.example.fivechef.WebChef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

@EnableJpaAuditing
@SpringBootApplication
public class WebChefApplication {

	public static void main(String[] args) {
		loadEnvFile();

		SpringApplication.run(WebChefApplication.class, args);

		System.out.println("-- Web Chef start --");
	}

	private static void loadEnvFile() {
		Path envPath = Path.of(".env");

		if (!Files.exists(envPath)) {
			System.out.println(".env 파일이 없습니다. 시스템 환경변수를 사용합니다.");
			return;
		}

		try (BufferedReader reader = Files.newBufferedReader(envPath)) {
			String line;

			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty()) {
					continue;
				}

				if (line.startsWith("#")) {
					continue;
				}

				int equalsIndex = line.indexOf("=");

				if (equalsIndex <= 0) {
					continue;
				}

				String key = line.substring(0, equalsIndex).trim();
				String value = line.substring(equalsIndex + 1).trim();

				if (key.isEmpty()) {
					continue;
				}

				if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
					value = value.substring(1, value.length() - 1);
				}

				if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
					value = value.substring(1, value.length() - 1);
				}

				System.setProperty(key, value);
			}

			System.out.println(".env 파일 로드 완료");

		} catch (Exception e) {
			System.out.println(".env 파일을 읽는 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
	}
}