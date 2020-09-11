package com.wine.to.up.demo.service.logging;

import com.wine.to.up.commonlib.logging.NotableEvent;

//TODO create-service: rename to reflect your service name. F.e OrderServiceNotableEvents
public enum DemoServiceNotableEvents implements NotableEvent {
    //TODO create-service: replace
    SOME_DEMO_EVENT("Something happened");

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
