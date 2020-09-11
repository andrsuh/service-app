//TODO create-service: move classes to correct package. F.e. for order-service all classes should be in "com.wine.to.up.order.service.*"
package com.wine.to.up.demo.service.components;

import com.wine.to.up.commonlib.metrics.CommonMetricsCollector;
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
}
