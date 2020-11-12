//TODO create-service: move classes to correct package. F.e. for order-service all classes should be in "com.wine.to.up.order.service.*"
package com.wine.to.up.demo.service.components;

import com.wine.to.up.commonlib.metrics.CommonMetricsCollector;
import io.micrometer.core.instrument.Metrics;
import org.springframework.stereotype.Component;

/**
 * This Class expose methods for recording specific metrics
 * It changes metrics of Micrometer and Prometheus simultaneously
 * Micrometer's metrics exposed at /actuator/prometheus
 * Prometheus' metrics exposed at /metrics-prometheus
 *
 */
//TODO create-service: rename
@Component
public class DemoServiceMetricsCollector extends CommonMetricsCollector {
    private static final String SERVICE_NAME = "demo_service";


    private static final String PARSING_STARTED_COUNTER = "parsing_started";
    private static final String PARSING_COMPLETE_COUNTER = "parsing_complete";
    private static final String PARSING_FAILED_COUNTER = "parsing_failed";

    public  static final String PARSER_NAME_TAG = "parser_name";

    public DemoServiceMetricsCollector() {
        this(SERVICE_NAME);
    }

    private DemoServiceMetricsCollector(String serviceName) {
        super(serviceName);
    }

    public void incParsingStarted(String parserName) {
        Metrics.counter(PARSING_STARTED_COUNTER, PARSER_NAME_TAG, parserName).increment();
    }

    public void incParsingComplete(String parserName) {
        Metrics.counter(PARSING_COMPLETE_COUNTER, PARSER_NAME_TAG, parserName).increment();
    }

    public void incParsingFailed(String parserName) {
        Metrics.counter(PARSING_FAILED_COUNTER, PARSER_NAME_TAG, parserName).increment();
    }
}
