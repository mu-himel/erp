package com.agi.aesl.erpscm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import java.util.TimeZone;


@EnableAsync
@SpringBootApplication
public class ErpScmApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"));

		SpringApplication.run(ErpScmApplication.class, args);
	}



}
