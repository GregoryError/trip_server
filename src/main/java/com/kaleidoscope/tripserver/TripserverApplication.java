package com.kaleidoscope.tripserver;

import org.hibernate.annotations.Comment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan
public class TripserverApplication {
	public static void main(String[] args) {
		SpringApplication.run(TripserverApplication.class, args);
	}

}
