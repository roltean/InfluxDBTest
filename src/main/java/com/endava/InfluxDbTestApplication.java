package com.endava;

import com.codahale.metrics.MetricRegistry;
import com.endava.reporter.InfluxDbReporter;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class InfluxDbTestApplication {

    private static final Logger LOG = LoggerFactory.getLogger(InfluxDbTestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(InfluxDbTestApplication.class, args);
    }

    @Bean
    public InfluxDbProperties influxDbProperties(){
        return new InfluxDbProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "influxdb.reporter", name = "enabled", matchIfMissing = true)
    public InfluxDbReporter influxDbReporter(Environment environment, InfluxDbProperties influxDbProperties, MetricRegistry metricRegistry){
        String applicationName = environment.getRequiredProperty("spring.application.name");
        String hostName = getHostname();

        InfluxDB influxDB = InfluxDBFactory.connect(influxDbProperties.getUrl(), influxDbProperties.getUser(), influxDbProperties.getPassword());
        BatchPoints batchPoints = BatchPoints
                .database(influxDbProperties.getDatabase())
                .retentionPolicy(influxDbProperties.getRetentionPolicy())
                .consistency(InfluxDB.ConsistencyLevel.QUORUM)
                .tag("application", applicationName)
                .tag("hostname", hostName)
                .build();
        final InfluxDbReporter reporter = InfluxDbReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.MINUTES)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(influxDB, batchPoints);
        reporter.start(5, TimeUnit.SECONDS);
        return reporter;
    }

    private String getHostname() {
        String hostName = "Unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warn("Unable to get hostname. Using default hostname 'unknown'", e);
        }
        return hostName;
    }
}
