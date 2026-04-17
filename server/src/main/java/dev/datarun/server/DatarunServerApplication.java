package dev.datarun.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatarunServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatarunServerApplication.class, args);
    }
}
