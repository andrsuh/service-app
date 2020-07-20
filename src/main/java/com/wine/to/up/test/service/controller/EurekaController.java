package com.wine.to.up.test.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EurekaController {
    /**
     * Values from app properties, used to generate requests
     */
    @Value("${server.port}")
    private String port;

    @Value("${spring.application.name}")
    private String appId;

    @Value("${spring.cloud.client.hostname}")
    private String hostName;

    /**
     * restTemplate object with load balancing and discovery client included
     */
    @Autowired
    private RestTemplate balancingRestTemplate;


    /**
     * Sets instance a given status in eureka server.
     * UP - instance is ok
     * DOWN - instance is not responding for some reason??
     * OUT_OF_SERVICE - removes instance from discovery
     * @param status
     * @return
     */
    @RequestMapping("/set-status={status}")
    public String setStatus(@PathVariable("status") String status) {
        if ((status.equals("DOWN")) || (status.equals("UP")) || (status.equals("OUT_OF_SERVICE"))) {
            String url = "http://localhost:8761/eureka/apps/{app-id}/{instance-id}/status?value={status}";
            Map<String, String> params = new HashMap<>();
            params.put("app-id", appId.toUpperCase());
            params.put("instance-id", hostName + ":" + appId + ":" + port);
            params.put("status", status);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.put(url, Void.class, params);
            return String.format("Instance status is set to %s", status);
        }
        return "Not a valid status. Use UP, DOWN or OUT_OF_SERVICE";
    }
}
