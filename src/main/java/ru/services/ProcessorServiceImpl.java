package ru.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import ru.DescriptorUtils;
import ru.HttpRequest;
import ru.ProcessOs;
import ru.descriptor.Descriptor;
import ru.descriptor.Param;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

@Service
public class ProcessorServiceImpl implements ProcessorService {
    @Autowired
    private ResourceLoader loader;

    private final Consumer<BufferedReader> readerConsumer = reader -> {reader.lines().forEach(System.out::println);};

    @Autowired
    private HttpService httpService;

    @Autowired
    private ApplicationContext context;

    private boolean isProcessing = false;

    private List<Param> dynamicParams;

    @Override
    public void process(File file, String login, String password, String cameraId, String videoId, String nodeId) throws JAXBException, IOException, InterruptedException {
        Descriptor descriptor = DescriptorUtils.getDescriptor(loader);

        if(!file.exists()){
            boolean isFileCreated = file.createNewFile();
            if(!isFileCreated){
                throw new FileNotFoundException("The file has not been created");
            }
        }

        Param fileParam = new Param();
        fileParam.setName("file");
        fileParam.setValue(file.getPath());

        if(!Files.isDirectory(Paths.get(descriptor.getProcessor().getOutputFile())) && descriptor.getProcessor().getType().equals("Image")){
            Files.createDirectory(Paths.get(descriptor.getProcessor().getOutputFile()));
        }

        ProcessOs videoProcess = new ProcessOs(descriptor.getProcessor().getExecLine(Collections.singletonList(fileParam)));
        long period = 1000;
        try {
            videoProcess.startProcess(period, readerConsumer, readerConsumer);

            Files.delete(file.toPath());


        }
        catch (InterruptedException e){
            updateSourceVideo(login, password, videoId);
            throw new InterruptedException("Processing of video failed");
        }

        if(descriptor.getProcessor().getType().equals("Video")) {
            final String ffmpegCommand = "ffmpeg -i " + descriptor.getProcessor().getOutputFile() + " " + file.getPath() + " -v error";
            ProcessOs ffmpegProcess = new ProcessOs(ffmpegCommand);
            ffmpegProcess.startProcess(period, readerConsumer, readerConsumer);

            LinkedMultiValueMap<String, Object> videoBody = new LinkedMultiValueMap<>();
            FileSystemResource fileResource = new FileSystemResource(file);
            videoBody.add("file", fileResource);

            HttpRequest request = context.getBean(HttpRequest.class);
            request.init(login, password, HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + file.getPath());

            String videoStatus = httpService.send(request, videoBody);

            Files.delete(file.toPath());

            insertNewVideo(login, password, videoId, cameraId, file, videoStatus);
            updateSourceVideo(login, password, videoId);
            insertNewVideoProcessing(login, password, videoId, nodeId);
        }
        else if(descriptor.getProcessor().getType().equals("Image")){
            if(!Files.isDirectory(Paths.get(descriptor.getProcessor().getOutputFile()))){
                throw new IllegalArgumentException("Enter directory of images");
            }

            List<String> imageIds = insertNewImages(login, password, videoId, descriptor.getProcessor().getOutputFile());
            insertNewImageProcessings(login, password, imageIds, nodeId);
            updateSourceVideo(login, password, videoId);
        }
        else {
            throw new IllegalArgumentException("Wrong type of processor");
        }
    }

    private List<String> insertNewImages(String login, String password, String videoId, String directory) {
        File files = new File(directory);
        File[] images = files.listFiles();
        List<String> imageIds = new LinkedList<>();
        for(File image: images){
            LinkedMultiValueMap<String, Object> imageBody = new LinkedMultiValueMap<>();
            FileSystemResource fileResource = new FileSystemResource(image);
            imageBody.add("file", fileResource);

            HttpRequest request = context.getBean(HttpRequest.class);
            request.init(login, password, HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + image.getPath());
            System.out.println(fileResource.getFile().length());
            System.out.println(fileResource.getFile().getName());
            String imageStatus = httpService.send(request, imageBody);

            JsonObject imageJson = new JsonObject();
            imageJson.addProperty("name", image.getName());

            JsonObject fileDescriptor = new JsonObject();
            String fileDescriptorId = JsonParser.parseString(imageStatus).getAsJsonObject().get("id").getAsString();
            fileDescriptor.addProperty("id", fileDescriptorId);

            JsonObject parentVideo = new JsonObject();
            parentVideo.addProperty("id", videoId);

            imageJson.add("fileDescriptor", fileDescriptor);
            imageJson.add("parentVideo", parentVideo);

            HttpRequest videoRequest = context.getBean(HttpRequest.class);
            videoRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Image");
            String message = httpService.send(videoRequest, imageJson.toString());

            String imageId = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();
            imageIds.add(imageId);
        }

        return imageIds;
    }

    private void insertNewImageProcessings(String login, String password, List<String> imageIds, String nodeId){
        JsonObject imageProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", nodeId);

        JsonArray images = new JsonArray();
        for(String imageId: imageIds){
            JsonObject image = new JsonObject();
            image.addProperty("id", imageId);

            images.add(image);
        }

        imageProcessing.add("node", node);
        imageProcessing.add("images", images);

        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_ImageProcessing");
        String message = httpService.send(videoProcessingRequest, imageProcessing.toString());
    }

    private void updateSourceVideo(String login, String password, String videoId){
        JsonObject updateVideo = new JsonObject();
        updateVideo.addProperty("status", "ready");

        HttpRequest videoUpdate = context.getBean(HttpRequest.class);
        videoUpdate.init(login, password, HttpMethod.PUT, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video/" + videoId);
        String message = httpService.send(videoUpdate, updateVideo.toString());

    }

    private void insertNewVideo(String login, String password, String videoId, String cameraId, File file, String videoStatus){
        JsonObject video = new JsonObject();
        video.addProperty("id", videoId);
        JsonObject camera = new JsonObject();
        camera.addProperty("id", cameraId);
        String fileDescriptorId = JsonParser.parseString(videoStatus).getAsJsonObject().get("id").getAsString();
        JsonObject fileDescriptor = new JsonObject();
        fileDescriptor.addProperty("id", fileDescriptorId);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "processed " + file.getName());
        jsonObject.add("camera", camera);
        jsonObject.addProperty("parentName", file.getName());
        jsonObject.add("fileDescriptor", fileDescriptor);
        jsonObject.add("parentVideo", video);
        jsonObject.addProperty("status", "processed");

        HttpRequest videoRequest = context.getBean(HttpRequest.class);
        videoRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video");
        String message = httpService.send(videoRequest, jsonObject.toString());
    }

    private void insertNewVideoProcessing(String login, String password, String videoId, String nodeId){
        JsonObject videoProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", nodeId);

        JsonObject video = new JsonObject();
        video.addProperty("id", videoId);

        videoProcessing.add("node", node);
        videoProcessing.add("video", video);

        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_VideoProcessing");
        String message = httpService.send(videoProcessingRequest, videoProcessing.toString());
    }

    @Override
    public boolean isProcessing() {
        return isProcessing;
    }

}
