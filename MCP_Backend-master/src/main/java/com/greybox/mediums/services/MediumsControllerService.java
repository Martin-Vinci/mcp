package com.greybox.mediums.services;

import com.greybox.mediums.config.SchemaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import ug.ac.mak.java.logger.Log;

@Component("CoreServices")
public class MediumsControllerService implements ApplicationListener<ContextRefreshedEvent> {
    public static SchemaConfig schemaConfig;
    public static Log logHandler;

    @Autowired
    public void setSchemaConfig(SchemaConfig schemaConfig) {
        MediumsControllerService.schemaConfig = schemaConfig;
    }

    @Autowired
    public void setLogger(Log logHandler) {
        MediumsControllerService.logHandler = logHandler;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

    }
}
