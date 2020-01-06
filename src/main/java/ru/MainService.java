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

@Service
public class MainService {

    public void process(File file, String cameraId){
        try {
            file.createNewFile();
            JAXBContext context = JAXBContext.newInstance(Descriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File f = new File("src/main/resources/descriptor.xml");
            Descriptor descriptor = (Descriptor) unmarshaller.unmarshal(f);
            descriptor.getParams().get(2).setValue(file.getPath());
            StringBuilder w = new StringBuilder();
            w.append("./")
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
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            //Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + "asfasf" + file.getPath());
            FileSystemResource value = new FileSystemResource(file);
            System.out.println(value.getFile().length());
            map.add("data", value);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("Authorization", "Bearer d733f9dd-1fbf-4488-8dcd-0ca8105c42c2");
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            System.out.println( restTemplate.exchange("http://localhost:8081/rest/v2/files/?name=" + file.getName() + "q", HttpMethod.POST, requestEntity, String.class).getBody());
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
