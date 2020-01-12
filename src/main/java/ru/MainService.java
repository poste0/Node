package ru;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiConsumer;

@Service
public class MainService {

    public void process(File file, String cameraId){
        try {
            file.createNewFile();
            JAXBContext context = JAXBContext.newInstance(Descriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File f = new File("src/main/resources/descriptor.xml");
            Descriptor descriptor = (Descriptor) unmarshaller.unmarshal(f);
            descriptor.getParams().get(0).setValue(file.getPath());
            StringBuilder w = new StringBuilder();
            w.append("python3 ")
                    .append(descriptor.getPathToCommand() + descriptor.getCommand() + " ");
            descriptor.getParams().forEach(d ->{
                if(!d.getValue().isEmpty()) {
                    w.append(d.getName() + " " + descriptor.getPathToCommand() + d.getValue() + " ");
                }
                else{
                    w.append(d.getName() + " ");
                }
            });

            System.out.println(w);
            Process process = Runtime.getRuntime().exec(w.toString());
            BufferedReader e = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while(process.isAlive()){
                System.out.println(e.readLine());
            }

            HttpHeaders headersm = new HttpHeaders();
            headersm.add("Authorization", "Basic Y2xpZW50OnNlY3JldA==");
            headersm.add("Content-Type", "application/x-www-form-urlencoded");
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + "asfasf" + file.getPath() + " -v error");
            BufferedReader i = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            BufferedReader er = new BufferedReader(new InputStreamReader(process1.getErrorStream()));
            while(process1.isAlive()){
                continue;
            }
            File result = new File("asfasf" + file.getPath());
            FileSystemResource value = new FileSystemResource(result);
            System.out.println(value.getFile().length());
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
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
