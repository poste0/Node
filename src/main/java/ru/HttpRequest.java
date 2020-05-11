package ru;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

@Component
@Scope("prototype")
public class HttpRequest {
    private String token;

    private HttpHeaders headers;

    private HttpMethod method;

    private String address;

    @Autowired
    private ResourceLoader loader;

    public HttpRequest(){}

    public void init(String login, String password, HttpMethod method, MediaType type, String endPoint) throws RestClientException, NullPointerException {
        try {
            this.address = DescriptorUtils.getDescriptor(loader).getNode().getServer() + endPoint;

            getToken(login, password);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.method = method;
        this.headers = new HttpHeaders();
        this.headers.add("Authorization", "Bearer " + token);
        this.headers.setContentType(type);
    }

    private void getToken(String login, String password) throws RestClientException, NullPointerException, JAXBException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic Y2xpZW50OnNlY3JldA==");
        headers.add("Content-Type", "application/x-www-form-urlencoded");

        LinkedMultiValueMap<String, Object> token = new LinkedMultiValueMap<>();
        token.add("grant_type", "password");
        token.add("username", login);
        token.add("password", password);

        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(token, headers);

        RestTemplate template = new RestTemplate();
        String message = template.exchange(DescriptorUtils.getDescriptor(loader).getNode().getServer() + "/app/rest/v2/oauth/token", HttpMethod.POST, request, String.class).getBody();
        String result = message.split("\"")[3];

        this.token = result;


    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public HttpHeaders getHeaders(){
        return this.headers;
    }

    public void setHeaders(HttpHeaders headers){
        this.headers = headers;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){
        this.address = address;
    }
}
