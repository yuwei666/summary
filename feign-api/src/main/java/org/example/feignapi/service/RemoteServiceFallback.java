package org.example.feignapi.service;

public class RemoteServiceFallback implements RemoteService {

    @Override
    public String getData(String id) {
        return "fallback";
    }

    @Override
    public String createData(String requestBody) {
        return "fallback";
    }
}
