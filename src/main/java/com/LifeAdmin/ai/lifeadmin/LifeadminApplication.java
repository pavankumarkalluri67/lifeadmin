package com.LifeAdmin.ai.lifeadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LifeadminApplication {

	public static void main(String[] args) {
		// Keep compatibility with newer JDKs where sun.misc.Unsafe memory APIs are being removed.
		System.setProperty("io.netty.noUnsafe", System.getProperty("io.netty.noUnsafe", "true"));
		SpringApplication.run(LifeadminApplication.class, args);
	}

}
