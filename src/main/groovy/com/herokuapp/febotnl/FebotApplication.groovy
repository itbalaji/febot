package com.herokuapp.febotnl

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@Configuration
@EnableWebMvc
@ComponentScan('com.herokuapp.febotnl')
public class FebotApplication {
	@Bean(name='messengerRestTemplate')
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build()
	}

	public static void main(String[] args) {
		SpringApplication.run(FebotApplication, args)
	}
}
