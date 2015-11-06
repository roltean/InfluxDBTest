package com.endava;

import com.codahale.metrics.*;
import metrics_influxdb.InfluxdbReporter;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by roltean on 05.11.2015.
 */
public class InfluxDbReporter extends ScheduledReporter {

    private final InfluxDB influxdb;
    private final BatchPoints batchPoints;
    private final Clock clock;
    private final String prefix;
    private final boolean skipIdleMetrics;

    public InfluxDbReporter(MetricRegistry registry,
                            InfluxDB influxdb,
                            BatchPoints batchPoints,
                            Clock clock,
                            String prefix,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter,
                            boolean skipIdleMetrics) {
        super(registry, "influxdb-reporter", filter, rateUnit, durationUnit);
        this.influxdb = influxdb;
        this.batchPoints = batchPoints;
        this.clock = clock;
        this.prefix = (prefix == null) ? "" : (prefix.trim() + ".");
        this.skipIdleMetrics = skipIdleMetrics;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime();
        reportCounters(counters, timestamp);
        influxdb.write(batchPoints);
    }

    private void reportCounters(SortedMap<String, Counter> counters, long timestamp) {
        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            Point count = Point.measurement(entry.getKey())
                    .time(timestamp, TimeUnit.MILLISECONDS)
                    .field("count", entry.getValue().getCount())
                    .build();
            batchPoints.point(count);
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
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private boolean skipIdleMetrics;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
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
         * Only report metrics that have changed.
         *
         * @param skipIdleMetrics
         * @return {@code this}
         */
        public Builder skipIdleMetrics(boolean skipIdleMetrics) {
            this.skipIdleMetrics = skipIdleMetrics;
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
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter,
                    skipIdleMetrics);
        }


    }
}
