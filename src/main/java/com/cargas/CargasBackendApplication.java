package com.cargas;

import com.cargas.core.Database;
import com.cargas.core.MongoBase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CargasBackendApplication {

	private static Database database;

	public static void main(String[] args) {
		database = new MongoBase();
		database.init();

		SpringApplication.run(CargasBackendApplication.class, args);
	}

	public static Database getDatabase(){
		return database;
	}
}
