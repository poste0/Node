package ru;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class MainService {

    @Autowired
    private ResourceLoader loader;

    public void process(File file, String login, String password){
        try {
            file.createNewFile();
            Descriptor descriptor = getDescriptor();
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
            Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + "asfasf" + file.getPath() + ".mp4 -v error");
            BufferedReader i = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            BufferedReader er = new BufferedReader(new InputStreamReader(process1.getErrorStream()));
            while(process1.isAlive()){
                System.out.println("ffmpeg");
                System.out.println(er.readLine());
            }
            File result = new File("asfasf" + file.getPath() + ".mp4");
            FileSystemResource value = new FileSystemResource(result);
            System.out.println(value.getFile().length());
            map.add("file", value);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String token = "";
            LinkedMultiValueMap<String, Object> tokenm = new LinkedMultiValueMap<>();
            tokenm.add("grant_type", "password");
            tokenm.add("username", login);
            tokenm.add("password", password);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestm = new HttpEntity<>(tokenm, headersm);
            RestTemplate templatem = new RestTemplate();
            String message = templatem.exchange("http://localhost:8081/app/rest/v2/oauth/token", HttpMethod.POST, requestm, String.class).getBody();
            token = message.split("\"")[3];
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            System.out.println( restTemplate.exchange("http://localhost:8081/app/rest/v2/files/?name=" + result.getPath() , HttpMethod.POST, requestEntity, String.class).getBody());

            Files.delete(result.toPath());
            Files.delete(file.toPath());

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Descriptor getDescriptor() throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(Descriptor.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        File f = new File("descriptor.xml");
        try {
            FileUtils.copyInputStreamToFile(loader.getResource("classpath:descriptor.xml").getInputStream(), f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Descriptor descriptor = (Descriptor) unmarshaller.unmarshal(f);

        return descriptor;
    }

    public String getCpu(){
        try {
            Descriptor descriptor = getDescriptor();
            List<Param> cpuParam = descriptor.getParams().stream().filter((param -> {
                return param.getName().equals("CPU");
            })).collect(Collectors.toList());

            if(cpuParam.size() != 1){
                throw new IllegalStateException();
            }
            return cpuParam.get(0).getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    public String getGpu(){
        try {
            Descriptor descriptor = getDescriptor();
            List<Param> gpuParam = descriptor.getParams().stream().filter((param -> {
                return param.getName().equals("GPU");
            })).collect(Collectors.toList());

            if(gpuParam.size() != 1){
                throw new IllegalStateException();
            }
            return gpuParam.get(0).getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}
