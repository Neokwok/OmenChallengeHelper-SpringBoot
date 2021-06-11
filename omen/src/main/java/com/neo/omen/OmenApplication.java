package com.neo.omen;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@Slf4j
@SpringBootApplication
@MapperScan("com.neo.omen.mapper")
public class OmenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmenApplication.class, args);
        log.info("程序成功启动，请在浏览器中打开 http://localhost:8080/omen 启动批量完成操作");
    }

}
