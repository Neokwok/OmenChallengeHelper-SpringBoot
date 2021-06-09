package com.neo.omen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.neo.omen.mapper")
public class OmenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmenApplication.class, args);
    }

}
