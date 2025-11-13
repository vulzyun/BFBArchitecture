package com.BFBManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.BFBManagement.domain",
	"com.BFBManagement.application",
	"com.BFBManagement.adapters"
})
public class BfbManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(BfbManagementApplication.class, args);
	}

}
