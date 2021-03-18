package ru.processor;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import ru.HttpRequest;
import ru.ProcessOs;
import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.Descriptor;
import ru.processor.exception.PreProcessException;
import ru.processor.exception.SendFileException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class VideoProcessor extends AbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(VideoProcessor.class);

    private String videoFileInfo;

    public VideoProcessor(UserData userData, VideoData videoData, ResourceLoader loader) throws FileNotFoundException {
        super(userData, videoData, loader);
    }

    @Override
    protected UUID insertNewProcessing() {
        log.info("Video processing is sent to the platform");

        JsonObject videoProcessingJson = createVideoProcessingJson();

        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_VideoProcessing");
        String message = httpService.send(videoProcessingRequest, videoProcessingJson.toString());

        final String videoProcessingId = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();;
        log.info("Video processing with id {} has been created", videoProcessingId);


        return UUID.fromString(videoProcessingId);
    }

    private JsonObject createVideoProcessingJson(){
        JsonObject videoProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", videoData.getNodeId().toString());

        JsonObject video = new JsonObject();
        video.addProperty("id", videoData.getVideoId().toString());

        videoProcessing.add("node", node);
        videoProcessing.add("video", video);

        return videoProcessing;
    }

    @Override
    protected void insertNewData() {
        log.info("Result video is sent to the platform");

        JsonObject jsonObject = createVideoJson();

        HttpRequest videoRequest = context.getBean(HttpRequest.class);
        videoRequest.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video");
        String message = httpService.send(videoRequest, jsonObject.toString());

        log.info("Video of video processing with id {} has sent to the platform", processingId.toString());
    }

    private JsonObject createVideoJson() throws JsonIOException{
        JsonObject video = new JsonObject();
        video.addProperty("id", videoData.getVideoId().toString());

        JsonObject camera = new JsonObject();
        camera.addProperty("id", videoData.getCameraId().toString());
        String fileDescriptorId = JsonParser.parseString(videoFileInfo).getAsJsonObject().get("id").getAsString();

        JsonObject fileDescriptor = new JsonObject();
        fileDescriptor.addProperty("id", fileDescriptorId);

        JsonObject videoProcessing = new JsonObject();
        videoProcessing.addProperty("id", processingId.toString());

        String message = "";
        if(isTextMessageUsed()){
            try {
                message = getMessage(videoData.getVideoFile());
            }
            catch (FileExistsException e){
                throw new JsonIOException("No message");
            }
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("name", "processed " + videoData.getVideoFile().getName());
        jsonObject.add("camera", camera);
        jsonObject.addProperty("parentName", videoData.getVideoFile().getName());
        jsonObject.add("fileDescriptor", fileDescriptor);
        jsonObject.add("parentVideo", video);
        jsonObject.addProperty("status", "processed");
        jsonObject.add("videoProcessing", videoProcessing);

        return jsonObject;
    }

    @Override
    protected void preprocess() throws PreProcessException {
        try {
            convertVideo();
            sendVideoFile();
            deleteVideoFile();
        } catch (IOException | InterruptedException e) {
            throw new PreProcessException(e.getMessage());
        }
    }

    private void convertVideo() throws IOException, InterruptedException {
        final long period = 1000;
        final Consumer<BufferedReader> readerConsumer = reader -> {
            reader.lines().forEach(System.out::println);
        };

        final String ffmpegCommand = "ffmpeg -i " + descriptor.getProcessor().getOutputFile() + " " + videoData.getVideoFile().getPath() + " -v error";
        ProcessOs ffmpegProcess = new ProcessOs(ffmpegCommand);
        ffmpegProcess.startProcess(period, readerConsumer, readerConsumer);
        log.info("Process with command {} has started", ffmpegProcess.getCommand());

    }

    private void sendVideoFile() throws SendFileException{
        log.info("Request to the platform is being created");

        try {
            LinkedMultiValueMap<String, Object> videoBody = new LinkedMultiValueMap<>();
            FileSystemResource fileResource = new FileSystemResource(videoData.getVideoFile());
            videoBody.add("file", fileResource);

            HttpRequest request = context.getBean(HttpRequest.class);
            request.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + videoData.getVideoFile().getPath());

            videoFileInfo = httpService.send(request, videoBody);
            log.info("Request with address {}, method {} has sent", request.getAddress(), request.getMethod());
        }
        catch (BeansException | RestClientException | NullPointerException e){
            throw new SendFileException(e.getMessage());
        }

    }

    private void deleteVideoFile() throws IOException{
        Files.delete(videoData.getVideoFile().toPath());
        log.info("File with video has been deleted");
    }
}
