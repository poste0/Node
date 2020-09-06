package ru.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.HttpRequest;

public interface HttpService {
    default String send(HttpRequest request, Object body) throws RestClientException {
        HttpEntity entity = new HttpEntity(body, request.getHeaders());

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody(false);

        RestTemplate restTemplate = new RestTemplate(factory);

        String result = restTemplate.exchange(request.getAddress(), request.getMethod(), entity, String.class).getBody();

        return result;
    }
}
