package com.agi.aesl.erpscm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.TimeZone;


@EnableAsync
@SpringBootApplication
public class ErpScmApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+06:00"));
		SpringApplication.run(ErpScmApplication.class, args);
	}



}
