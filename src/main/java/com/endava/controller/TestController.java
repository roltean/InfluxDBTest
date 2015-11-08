package com.endava.controller;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project:   InfluxDBTest
 * Copyright: Deutsche Telekom AG
 *
 * @author Robert Winkler <robert.winkler@telekom.de>
 * @since 2.0.0
 */
@RestController
public class TestController {

    private static final Logger LOG = LoggerFactory.getLogger(TestController.class);

    private final Counter counter;
    private final Timer timer;

    @Autowired
    public TestController(MetricRegistry metricRegistry){
        this.counter = metricRegistry.counter("testCounter");
        this.timer = metricRegistry.timer("com.endava.controller.TestController.increaseMeter");
    }

    @RequestMapping(value = "/counter", method = RequestMethod.POST)
    public void increaseCounter(){
        counter.inc();
    }

    @RequestMapping(value = "/timer", method = RequestMethod.POST)
    public void increaseMeter(){
        final Timer.Context context = timer.time();
        try {
            Thread.sleep(Double.valueOf(Math.random() * 1000).longValue());
        } catch (InterruptedException e) {
            LOG.warn("Failed to sleep", e);
        } finally {
            context.stop();
        }
    }
}
