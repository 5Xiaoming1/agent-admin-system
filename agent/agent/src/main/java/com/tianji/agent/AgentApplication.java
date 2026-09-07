package com.tianji.agent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("========================================");
        System.out.println("  Agent 应用启动成功！");
        System.out.println("  Knife4j 文档: http://localhost:8081/doc.html");
        System.out.println("  Swagger UI:  http://localhost:8081/swagger-ui/index.html");
        System.out.println("========================================");
    }

}