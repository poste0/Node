package ru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        File result = new File("asfasf151.mp4");
        FileSystemResource value = new FileSystemResource(result);
        System.out.println(value.getFile().length());
        HttpHeaders headersm = new HttpHeaders();
        headersm.add("Authorization", "Basic Y2xpZW50OnNlY3JldA==");
        headersm.add("Content-Type", "application/x-www-form-urlencoded");
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", value);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String token = "";
        LinkedMultiValueMap<String, Object> tokenm = new LinkedMultiValueMap<>();
        tokenm.add("grant_type", "password");
        tokenm.add("username", "admin");
        tokenm.add("password", "admin");
        HttpEntity<LinkedMultiValueMap<String, Object>> requestm = new HttpEntity<>(tokenm, headersm);
        RestTemplate templatem = new RestTemplate();
        String message = templatem.exchange("http://localhost:8081/app/rest/v2/oauth/token", HttpMethod.POST, requestm, String.class).getBody();
        token = message.split("\"")[3];
        headers.add("Authorization", "Bearer " + token);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println( restTemplate.exchange("http://localhost:8081/app/rest/v2/files/?name=" + result.getPath() , HttpMethod.POST, requestEntity, String.class).getBody());
        SpringApplication.run(Main.class);
    }
}
