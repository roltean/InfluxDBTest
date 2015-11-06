package com.endava;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Created by roltean on 05.11.2015.
 */
@Component
public class InfluxDBComponent implements CommandLineRunner {

    private final MetricRegistry metricRegistry;

    @Autowired
    public InfluxDBComponent(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void run(String... args) throws Exception {
        Counter counter = metricRegistry.counter("testCounter");

        InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.33.21:8086", "root", "root");
        BatchPoints batchPoints = BatchPoints
                .database("monitor")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.QUORUM)
                .build();
        final InfluxDbReporter reporter = InfluxDbReporter.forRegistry(metricRegistry)
                .build(influxDB, batchPoints);
        reporter.start(5, TimeUnit.SECONDS);

        while(true){
            counter.inc();
            Thread.sleep(1000);
        }
    }
}
