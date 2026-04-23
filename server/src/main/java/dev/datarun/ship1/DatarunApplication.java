package dev.datarun.ship1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ship-1 entry point. Delivers scenarios S00 + S01 + S03 per docs/ships/ship-1.md.
 */
@SpringBootApplication
public class DatarunApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatarunApplication.class, args);
    }
}
