package org.balczukiewicz.notekeeperservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class NoteKeeperServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteKeeperServiceApplication.class, args);
    }
}