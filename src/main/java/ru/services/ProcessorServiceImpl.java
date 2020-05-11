package ru.services;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ProcessorServiceImpl implements ProcessorService {
    @Autowired
    private ResourceLoader loader;

    private final long period = 1000;

    private final Consumer<BufferedReader> readerConsumer = reader -> {reader.lines().forEach(System.out::println);};

    @Autowired
    private HttpService httpService;

    @Autowired
    private ApplicationContext context;

    private boolean isProcessing = false;

    private List<Param> dynamicParams;

    @Override
    public void process(File file, String login, String password, String cameraId, String videoId) throws JAXBException, IOException, InterruptedException {
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
        ProcessOs videoProcess = new ProcessOs(descriptor.getProcessor().getExecLine(Collections.singletonList(fileParam)));
        try {
            videoProcess.startProcess(period, readerConsumer, readerConsumer);
        } catch (InterruptedException e) {
            throw new InterruptedException("Processing of video failed");
        }

        Files.delete(file.toPath());

        final String ffmpegCommand = "ffmpeg -i " + descriptor.getProcessor().getOutputFile() + " " + file.getPath() + " -v error";
        ProcessOs ffmpegProcess = new ProcessOs(ffmpegCommand);
        try {
            ffmpegProcess.startProcess(period, readerConsumer, readerConsumer);
        }
        catch (InterruptedException e){
            throw new InterruptedException("FFmpeg failed");
        }

        LinkedMultiValueMap<String, Object> videoBody = new LinkedMultiValueMap<>();
        FileSystemResource fileResource = new FileSystemResource(file);
        videoBody.add("file", fileResource);

        HttpRequest request = context.getBean(HttpRequest.class);
        request.init(login, password, HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + file.getPath());

        String videoStatus = httpService.send(request, videoBody);

        Files.delete(file.toPath());

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

        JsonObject updateVideo = new JsonObject();
        updateVideo.addProperty("status", "ready");
        updateVideo.addProperty("node", "null");

        HttpRequest videoUpdate = context.getBean(HttpRequest.class);
        videoUpdate.init(login, password, HttpMethod.PUT, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video/" + videoId);



    }

    @Override
    public boolean isProcessing() {
        return isProcessing;
    }

}
