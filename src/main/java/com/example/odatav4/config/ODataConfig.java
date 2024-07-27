package com.example.odatav4.config;

import com.example.odatav4.processor.AcqAccountProcessor;
import com.example.odatav4.processor.AcqContactProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@Configuration
public class ODataConfig {

    @Autowired
    private AcqAccountProcessor acqAccountProcessor;

    @Autowired
    private AcqContactProcessor acqContactProcessor;

    @Autowired
    private MyEdmProvider myEdmProvider;

    @Bean
    public ODataHttpHandler oDataHttpHandler(HttpServletRequest request, HttpServletResponse response) {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(myEdmProvider, new ArrayList<>());
        ODataHttpHandler handler = odata.createHandler(edm);
        handler.register(acqAccountProcessor);
        handler.register(acqContactProcessor);
        handler.register(new DefaultProcessor());
        return handler;
    }
}
