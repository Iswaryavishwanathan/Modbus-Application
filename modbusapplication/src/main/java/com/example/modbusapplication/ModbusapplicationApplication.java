package com.example.modbusapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModbusapplicationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModbusapplicationApplication.class, args);
	}

}
