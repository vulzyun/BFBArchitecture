package com.BFBManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.BFBManagement.business",
	"com.BFBManagement.infrastructures",
	"com.BFBManagement.interfaces"
})
public class BfbManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(BfbManagementApplication.class, args);
	}

}
