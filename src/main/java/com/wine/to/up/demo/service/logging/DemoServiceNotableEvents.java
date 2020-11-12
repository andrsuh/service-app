package com.wine.to.up.demo.service.logging;

import com.wine.to.up.commonlib.logging.NotableEvent;

//TODO create-service: rename to reflect your service name. F.e OrderServiceNotableEvents
public enum DemoServiceNotableEvents implements NotableEvent {
    I_KAFKA_SEND_MESSAGE_SUCCESS("Kafka send message: {}"),
    I_CONTROLLER_RECEIVED_MESSAGE("Message: {}"),
    W_SOME_WARN_EVENT("Warn situation. Description: {}"),

    W_PAGE_PARSING_FAILED("Page parsing failed. Url : {}"),
    E_HTML_PAGE_FETCHING_FAILED("Did not manage to fetch url : {}"),

    DEV_UNEXPECTED_ERROR("Unexpected : {}"),
    ;

    private final String template;

    DemoServiceNotableEvents(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return name();
    }


}
