package com.endava;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
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

        counter.inc();
        counter.inc();

        /*
        final InfluxdbHttp influxdb = new InfluxdbHttp("192.168.33.21", 8086, "monitor", "root", "root"); // http transport
        // = new InfluxDbUdp("127.0.0.1", 1234); // udp transport
        //influxdb.debugJson = true; // to print json on System.err
        //influxdb.jsonBuilder = new MyJsonBuildler(); // to use MyJsonBuilder to create json
        final InfluxdbReporter reporter = InfluxdbReporter
                .forRegistry(metricRegistry)
                .prefixedWith("test")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .skipIdleMetrics(true) // Only report metrics that have changed.
                .build(influxdb);
        reporter.start(5, TimeUnit.SECONDS);
        */

        InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.33.21:8086", "root", "root");
        String dbName = "monitor";
        //influxDB.createDatabase(dbName);

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.QUORUM)
                .build();
        Point point1 = Point.measurement("testCounter")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("value", counter.getCount())
                .tag("application", "influxdbtest")
                .build();

        batchPoints.point(point1);
        influxDB.write(batchPoints);
    }
}
