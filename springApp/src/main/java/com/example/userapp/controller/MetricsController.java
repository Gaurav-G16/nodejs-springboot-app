package com.example.userapp.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.userapp.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class MetricsController {
    
    private final MeterRegistry meterRegistry;
    private final UserService userService;
    
    public MetricsController(MeterRegistry meterRegistry, UserService userService) {
        this.meterRegistry = meterRegistry;
        this.userService = userService;
    }
    
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Real-time user data
        long actualUserCount = userService.getAllUsers().size();
        metrics.put("userapp_users_registered_total", (double) actualUserCount);
        
        // Registration metrics
        Counter registrationCounter = meterRegistry.find("users.registered.total").counter();
        metrics.put("userapp_users_registration_attempts", registrationCounter != null ? registrationCounter.count() : 0.0);
        
        Counter failedRegistrations = meterRegistry.find("users.registration.failed").counter();
        metrics.put("userapp_users_registration_failures", failedRegistrations != null ? failedRegistrations.count() : 0.0);
        
        Counter deletedUsers = meterRegistry.find("users.deleted.total").counter();
        metrics.put("userapp_users_deleted_total", deletedUsers != null ? deletedUsers.count() : 0.0);
        
        // HTTP request metrics by endpoint
        double totalRequests = 0;
        double getUserRequests = getHttpRequestCount("/users", "GET");
        double postUserRequests = getHttpRequestCount("/users", "POST");
        double webFormRequests = getHttpRequestCount("/", "GET");
        double webSubmitRequests = getHttpRequestCount("/register", "POST");
        
        metrics.put("userapp_http_requests_get_users", getUserRequests);
        metrics.put("userapp_http_requests_post_users", postUserRequests);
        metrics.put("userapp_http_requests_web_form", webFormRequests);
        metrics.put("userapp_http_requests_web_submit", webSubmitRequests);
        
        totalRequests = getUserRequests + postUserRequests + webFormRequests + webSubmitRequests;
        metrics.put("userapp_http_requests_total", totalRequests);
        
        // HTTP status code metrics
        metrics.put("userapp_http_requests_2xx", getHttpRequestCountByStatus("2xx"));
        metrics.put("userapp_http_requests_4xx", getHttpRequestCountByStatus("4xx"));
        metrics.put("userapp_http_requests_5xx", getHttpRequestCountByStatus("5xx"));
        
        // Response time metrics
        Timer userSaveTimer = meterRegistry.find("user.save.time").timer();
        if (userSaveTimer != null) {
            metrics.put("userapp_user_save_avg_ms", userSaveTimer.mean(TimeUnit.MILLISECONDS));
            metrics.put("userapp_user_save_max_ms", userSaveTimer.max(TimeUnit.MILLISECONDS));
            metrics.put("userapp_user_save_count", (double) userSaveTimer.count());
        }
        
        Timer userFetchTimer = meterRegistry.find("user.fetch.time").timer();
        if (userFetchTimer != null) {
            metrics.put("userapp_user_fetch_avg_ms", userFetchTimer.mean(TimeUnit.MILLISECONDS));
            metrics.put("userapp_user_fetch_max_ms", userFetchTimer.max(TimeUnit.MILLISECONDS));
            metrics.put("userapp_user_fetch_count", (double) userFetchTimer.count());
        }
        
        // JVM metrics - detailed
        metrics.put("userapp_jvm_memory_used_bytes", getGaugeValue("jvm.memory.used"));
        metrics.put("userapp_jvm_memory_max_bytes", getGaugeValue("jvm.memory.max"));
        metrics.put("userapp_jvm_memory_committed_bytes", getGaugeValue("jvm.memory.committed"));
        metrics.put("userapp_jvm_heap_used_bytes", getGaugeValue("jvm.memory.used", "area", "heap"));
        metrics.put("userapp_jvm_nonheap_used_bytes", getGaugeValue("jvm.memory.used", "area", "nonheap"));
        
        metrics.put("userapp_jvm_threads_live", getGaugeValue("jvm.threads.live"));
        metrics.put("userapp_jvm_threads_daemon", getGaugeValue("jvm.threads.daemon"));
        metrics.put("userapp_jvm_threads_peak", getGaugeValue("jvm.threads.peak"));
        
        metrics.put("userapp_jvm_gc_pause_count", getTimerCount("jvm.gc.pause"));
        metrics.put("userapp_jvm_gc_pause_total_ms", getTimerTotalTime("jvm.gc.pause"));
        
        // Database metrics - detailed
        metrics.put("userapp_db_connections_active", getGaugeValue("hikaricp.connections.active"));
        metrics.put("userapp_db_connections_idle", getGaugeValue("hikaricp.connections.idle"));
        metrics.put("userapp_db_connections_pending", getGaugeValue("hikaricp.connections.pending"));
        metrics.put("userapp_db_connections_max", getGaugeValue("hikaricp.connections.max"));
        metrics.put("userapp_db_connections_min", getGaugeValue("hikaricp.connections.min"));
        
        // System metrics
        metrics.put("userapp_process_uptime_seconds", getGaugeValue("process.uptime"));
        metrics.put("userapp_process_cpu_usage", getGaugeValue("process.cpu.usage"));
        metrics.put("userapp_system_cpu_usage", getGaugeValue("system.cpu.usage"));
        metrics.put("userapp_system_load_average_1m", getGaugeValue("system.load.average.1m"));
        
        // Application health indicators
        double errorRate = totalRequests > 0 ? 
            (getHttpRequestCountByStatus("4xx") + getHttpRequestCountByStatus("5xx")) / totalRequests * 100 : 0;
        metrics.put("userapp_error_rate_percentage", errorRate);
        
        double memoryUsagePercentage = getGaugeValue("jvm.memory.max") > 0 ? 
            getGaugeValue("jvm.memory.used") / getGaugeValue("jvm.memory.max") * 100 : 0;
        metrics.put("userapp_memory_usage_percentage", memoryUsagePercentage);
        
        return metrics;
    }
    
    private double getHttpRequestCount(String uri, String method) {
        Counter counter = meterRegistry.find("http.server.requests")
                .tag("uri", uri)
                .tag("method", method)
                .counter();
        return counter != null ? counter.count() : 0.0;
    }
    
    private double getHttpRequestCountByStatus(String status) {
        Counter counter = meterRegistry.find("http.server.requests")
                .tag("status", status)
                .counter();
        return counter != null ? counter.count() : 0.0;
    }
    
    private double getGaugeValue(String name) {
        Gauge gauge = meterRegistry.find(name).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }
    
    private double getGaugeValue(String name, String tagKey, String tagValue) {
        Gauge gauge = meterRegistry.find(name).tag(tagKey, tagValue).gauge();
        return gauge != null ? gauge.value() : 0.0;
    }
    
    private double getTimerCount(String name) {
        Timer timer = meterRegistry.find(name).timer();
        return timer != null ? (double) timer.count() : 0.0;
    }
    
    private double getTimerTotalTime(String name) {
        Timer timer = meterRegistry.find(name).timer();
        return timer != null ? timer.totalTime(TimeUnit.MILLISECONDS) : 0.0;
    }
}
