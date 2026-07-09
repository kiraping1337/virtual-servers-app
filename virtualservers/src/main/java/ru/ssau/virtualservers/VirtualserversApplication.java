package ru.ssau.virtualservers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VirtualserversApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualserversApplication.class, args);
	}

}
