package ru.services;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.HttpRequest;

public interface HttpService {
    default String send(HttpRequest request, Object body){
        HttpEntity entity = new HttpEntity(body, request.getHeaders());

        RestTemplate restTemplate = new RestTemplate();

        String result = restTemplate.exchange(request.getAddress(), request.getMethod(), entity, String.class).getBody();

        return result;
    }
}
