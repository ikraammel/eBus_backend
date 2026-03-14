package com.gestion.gestion_bus;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestionBusApplication {

	public static void main(String[] args) {

		// Charger le fichier .env
		Dotenv dotenv = Dotenv.load();

		// Passer les variables à Spring
		System.setProperty("spring.datasource.url", dotenv.get("DB_URL"));
		System.setProperty("spring.datasource.username", dotenv.get("DB_USERNAME"));
		System.setProperty("spring.datasource.password", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(GestionBusApplication.class, args);
	}
}