package org.example.feignapi.service;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteServiceFactory implements FallbackFactory<RemoteService> {

    @Override
    public RemoteService create(Throwable cause) {
        return new RemoteService() {

            @Override
            public String getData(String id) {
                if (cause instanceof FeignException.NotFound) {
                    return "";
                } else {
                    return "Fallback User";
                }
            }

            @Override
            public String createData(String requestBody) {
                return "";
            }
        };
    }
}
