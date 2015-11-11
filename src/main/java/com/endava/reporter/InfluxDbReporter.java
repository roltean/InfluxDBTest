package com.endava.reporter;

import com.codahale.metrics.*;
import metrics_influxdb.InfluxdbReporter;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class InfluxDbReporter extends ScheduledReporter {

    private static final Logger LOG = LoggerFactory.getLogger(InfluxDbReporter.class);

    private final InfluxDB influxdb;
    private final BatchPoints batchPoints;
    private final Clock clock;

    public InfluxDbReporter(MetricRegistry registry,
                            InfluxDB influxdb,
                            BatchPoints batchPoints,
                            Clock clock,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "influxdb-reporter", filter, rateUnit, durationUnit);
        this.influxdb = influxdb;
        this.batchPoints = batchPoints;
        this.clock = clock;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        LOG.debug("Start reporting metrics to InfluxDB");
        final long timestamp = clock.getTime();
        reportCounters(counters, timestamp);
        reportTimers(timers, timestamp);
        influxdb.write(batchPoints);
        LOG.debug("Finish reporting metrics to InfluxDB");
    }

    private void reportTimers(SortedMap<String, Timer> timers, long timestamp) {
        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            String timerName = entry.getKey();
            Timer timer = entry.getValue();
            LOG.debug("Adding timer '{}'", timerName);

            String[] splittedTimer = timerName.split("\\.");
            String className = splittedTimer[splittedTimer.length - 2];
            LOG.debug("ClassName '{}'", className);
            String methodName = splittedTimer[splittedTimer.length - 1];
            LOG.debug("MethodName '{}'", methodName);

            double oneMinuteRate = convertRate(timer.getOneMinuteRate());
            double percentile98 = convertDuration(timer.getSnapshot().get98thPercentile());
            double percentile95 = convertDuration(timer.getSnapshot().get95thPercentile());
            double mean = convertDuration(timer.getSnapshot().getMean());
            long count = entry.getValue().getCount();

            LOG.debug("OneMinuteRate '{}'", oneMinuteRate);
            LOG.debug("98thPercentile '{}'", percentile98);
            LOG.debug("95thPercentile '{}'", percentile95);
            LOG.debug("Mean '{}'", percentile95);
            LOG.debug("Count '{}'", count);

            Point oneMinuteRatePoint = Point.measurement("oneMinuteRate")
                    .time(timestamp, TimeUnit.MILLISECONDS)
                    .field("oneMinuteRate", oneMinuteRate)
                    .tag("className", className)
                    .tag("methodName", methodName)
                    .build();
            Point responseTimePoint = Point.measurement("responseTime")
                    .time(timestamp, TimeUnit.MILLISECONDS)
                    .field("98thPercentile", percentile98)
                    .field("95thPercentile", percentile95)
                    .field("mean", mean)
                    .tag("className", className)
                    .tag("methodName", methodName)
                    .build();
            batchPoints.point(oneMinuteRatePoint);
            batchPoints.point(responseTimePoint);
        }
    }

    private void reportCounters(SortedMap<String, Counter> counters, long timestamp) {
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            LOG.debug("Adding counter '{}'", entry.getKey());
            Counter counter = entry.getValue();
            long count = counter.getCount();
            Point counterPoint = Point.measurement(entry.getKey())
                    .time(timestamp, TimeUnit.MILLISECONDS)
                    .field("count", count)
                    .build();
            batchPoints.point(counterPoint);
            counter.dec(counter.getCount());
        }
    }

    /**
     * Returns a new {@link Builder} for {@link InfluxdbReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link InfluxdbReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link InfluxdbReporter} instances. Defaults to not using a
     * prefix, using the default clock, converting rates to events/second,
     * converting durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link InfluxdbReporter} with the given properties, sending
         * metrics using the given {@link org.influxdb.InfluxDB} client.
         *
         * @param influxdb a {@link org.influxdb.InfluxDB} client
         * @param batchPoints a {@link org.influxdb.dto.BatchPoints} client
         * @return a {@link InfluxdbReporter}
         */
        public InfluxDbReporter build(InfluxDB influxdb, BatchPoints batchPoints) {
            return new InfluxDbReporter(registry,
                    influxdb,
                    batchPoints,
                    clock,
                    rateUnit,
                    durationUnit,
                    filter);
        }


    }
}
