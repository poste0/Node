package ru.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.springframework.web.client.RestTemplate;
import ru.descriptor.Descriptor;
import ru.descriptor.Param;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainService {

    @Autowired
    private ResourceLoader loader;

    private boolean isProcessing = false;

    public void process(File file, String login, String password, String cameraId, String videoId){
        /*try {
            isProcessing = true;
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
            Files.delete(file.toPath());
            Process process1 = Runtime.getRuntime().exec("ffmpeg -i " + "video.avi " + file.getPath() + " -v error");
            BufferedReader i = new BufferedReader(new InputStreamReader(process1.getInputStream()));
            BufferedReader er = new BufferedReader(new InputStreamReader(process1.getErrorStream()));
            while(process1.isAlive()){
                System.out.println("ffmpeg");
                System.out.println(er.readLine());
            }
            File result = new File(file.getPath());
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
            String address = descriptor.getParams().stream().filter(param -> {
                return param.getName().equals("Server");
            }).findFirst().get().getValue();
            HttpEntity<LinkedMultiValueMap<String, Object>> requestm = new HttpEntity<>(tokenm, headersm);
            RestTemplate templatem = new RestTemplate();
            String message = templatem.exchange(address + "app/rest/v2/oauth/token", HttpMethod.POST, requestm, String.class).getBody();
            token = message.split("\"")[3];
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            System.out.println();
            String serverMsg =  restTemplate.exchange(address + "/app/rest/v2/files/?name=" + result.getPath() , HttpMethod.POST, requestEntity, String.class).getBody();
            System.out.println(serverMsg);

            Files.delete(result.toPath());

            JsonObject video = new JsonObject();
            video.addProperty("id", videoId);
            JsonObject camera = new JsonObject();
            camera.addProperty("id", cameraId);
            String fileDescriptorId = JsonParser.parseString(serverMsg).getAsJsonObject().get("id").getAsString();
            JsonObject fileDescriptor = new JsonObject();
            fileDescriptor.addProperty("id", fileDescriptorId);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", "processed " + result.getName());
            jsonObject.add("camera", camera);
            jsonObject.addProperty("parentName", result.getName());
            jsonObject.add("fileDescriptor", fileDescriptor);
            jsonObject.add("parentVideo", video);
            jsonObject.addProperty("status", "processed");


            System.out.println(jsonObject.toString());
            message = templatem.exchange(address + "app/rest/v2/oauth/token", HttpMethod.POST, requestm, String.class).getBody();
            token = message.split("\"")[3];

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Authorization", "Bearer " + token);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity(jsonObject.toString(), httpHeaders);

            serverMsg = restTemplate.exchange(address + "/app/rest/v2/entities/platform_Video", HttpMethod.POST, httpEntity, String.class).getBody();

            message = templatem.exchange(address + "app/rest/v2/oauth/token", HttpMethod.POST, requestm, String.class).getBody();
            token = message.split("\"")[3];

            JsonObject updateVideo = new JsonObject();
            updateVideo.addProperty("status", "ready");
            HttpHeaders httpHeadersVideo = new HttpHeaders();
            httpHeadersVideo.add("Authorization", "Bearer " + token);
            httpHeadersVideo.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LinkedMultiValueMap<String, Object>> httpEntityVideo = new HttpEntity(updateVideo.toString(), httpHeadersVideo);

            serverMsg = restTemplate.exchange(address + "/app/rest/v2/entities/platform_Video/" + videoId, HttpMethod.PUT, httpEntityVideo, String.class).getBody();




        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            isProcessing = false;
        }

         */


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
        /*try {
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


         */


        return "Unknown";
    }

    public String getGpu(){
        /*try {
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

         */


        return "Unknown";
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }
}
