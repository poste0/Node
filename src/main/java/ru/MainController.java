package ru;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;

@RestController
public class MainController {

    @Autowired
    private MainService service;

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public ResponseEntity get(@RequestBody MultipartFile file){
        Executor executor = new ConcurrentTaskExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File result = new File(file.getOriginalFilename());
                try {
                    FileOutputStream writer = new FileOutputStream(result);
                    writer.write(file.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    file.transferTo(result);
                    result.createNewFile();
                    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                    //Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + "asfasf" + file.getPath());
                    FileSystemResource value = new FileSystemResource(result);
                    System.out.println(value.getFile().length());
                    map.add("file", value);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    String token = "";
                    LinkedMultiValueMap<String, Object> tokenm = new LinkedMultiValueMap<>();
                    HttpHeaders headersm = new HttpHeaders();
                    headersm.add("Authorization", "Basic Y2xpZW50OnNlY3JldA==");
                    headersm.add("Content-Type", "application/x-www-form-urlencoded");
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
                    System.out.println( restTemplate.exchange("http://localhost:8081/app/rest/v2/files/?name=" + file.getName() + "q", HttpMethod.POST, requestEntity, String.class).getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/g", method = RequestMethod.GET)
    public void g(@RequestParam("path") String path){
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        try {
            Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + "asfasf" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileSystemResource value = new FileSystemResource(new File("asfasf" + path));
        System.out.println(value.getFile().length());
        map.add("file", value);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange("http://localhost:8081/app-portal/file?cameraId=" + "cf0bbdfe-120c-b5f4-d47f-232beeae8f48", HttpMethod.POST, requestEntity, String.class);

    }
}
