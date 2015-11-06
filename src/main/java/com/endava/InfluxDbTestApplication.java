package com.endava;

import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InfluxDbTestApplication {

    @Autowired
    private MetricRegistry metricRegistry;

    public void start() {

    }

    public static void main(String[] args) {
        SpringApplication.run(InfluxDbTestApplication.class, args);
        
    }
}
