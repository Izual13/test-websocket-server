package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.math.BigInteger;
import java.security.SecureRandom;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("http://localhost:8080/");
        System.out.println("http://localhost:8080/start");
    }

    @Configuration
    @EnableWebSocketMessageBroker
    @EnableScheduling
    static class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            config.enableSimpleBroker("/topic", "/scheduler");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/socket").withSockJS();
        }

    }


    @RestController
    static class Controller {

        @Autowired
        MessagingService messagingService;

        @RequestMapping("/start")
        public String start() {
            messagingService.loginUser();
            return "start";
        }
    }

    @Configuration
    static class StaticResourceConfiguration extends WebMvcConfigurerAdapter {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        }

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/")
                    .setViewName("forward:/index.html");
        }
    }

    @Service
    static class MessagingService {
        @Autowired
        private MessageSendingOperations<String> messagingTemplate;

        public void loginUser() {
            String destination = "/topic";
            this.messagingTemplate.convertAndSend(destination, "ping");
        }

        @Scheduled(fixedDelay = 1000)
        public void scheduler() {
            String destination = "/scheduler";
            BigInteger prime = BigInteger.probablePrime(512, new SecureRandom());
            this.messagingTemplate.convertAndSend(destination, prime.toString());
        }
    }
}
