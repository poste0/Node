package ru.processor;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import ru.HttpRequest;
import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.Descriptor;
import ru.descriptor.TextMessage;
import ru.processor.exception.PreProcessException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ImageProcessor extends AbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);

    public ImageProcessor(UserData userData, VideoData videoData) throws FileNotFoundException {
        super(userData, videoData);
    }

    @Override
    protected UUID insertNewProcessing() {
        log.info("Image processing is sent to the platform");

        JsonObject imageProcessingJson = createImageProcessingJson();

        final String imageProcessingId = sendImageProcessing(imageProcessingJson);
        log.info("Image processing with id {} has been created", imageProcessingId);

        return UUID.fromString(imageProcessingId);
    }

    private JsonObject createImageProcessingJson(){
        JsonObject imageProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", videoData.getNodeId().toString());

        imageProcessing.add("node", node);

        return imageProcessing;
    }

    private String sendImageProcessing(JsonObject imageProcessingJson){
        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_ImageProcessing");

        String message = httpService.send(videoProcessingRequest, imageProcessingJson.toString());

        return JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();

    }

    @Override
    protected void insertNewData() {
        log.info("Images are sent to the platform");

        File[] images = getImageFiles(descriptor);

        for(File image: images){
            String imageStatus = sendImageFile(image);

            JsonObject imageJson = createImageJson(image, imageStatus);

            String message = sendImage(imageJson);

            log.info("Image of image processing with id {} has sent to the platform", processingId.toString());
        }

    }

    private File[] getImageFiles(Descriptor descriptor){
        File files = new File(descriptor.getProcessor().getOutputFile());

        return files.listFiles();
    }

    private String sendImageFile(File image){
        LinkedMultiValueMap<String, Object> imageBody = new LinkedMultiValueMap<>();
        FileSystemResource fileResource = new FileSystemResource(image);
        imageBody.add("file", fileResource);

        HttpRequest request = context.getBean(HttpRequest.class);
        request.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + image.getPath());

        return httpService.send(request, imageBody);
    }

    private JsonObject createImageJson(File image, String imageStatus){
        JsonObject imageJson = new JsonObject();
        imageJson.addProperty("name", image.getName());

        JsonObject fileDescriptor = new JsonObject();
        String fileDescriptorId = JsonParser.parseString(imageStatus).getAsJsonObject().get("id").getAsString();
        fileDescriptor.addProperty("id", fileDescriptorId);

        JsonObject parentVideo = new JsonObject();
        parentVideo.addProperty("id", videoData.getVideoId().toString());

        JsonObject imageProcessing = new JsonObject();
        imageProcessing.addProperty("id", processingId.toString());

        String message = "";

        if(isTextMessageUsed()){
            try {
                message = getMessage(image);
            }
            catch (FileExistsException e){
                throw new JsonIOException("No message");
            }
        }

        imageJson.addProperty("message", message);
        imageJson.add("fileDescriptor", fileDescriptor);
        imageJson.add("parentVideo", parentVideo);
        imageJson.add("imageProcessing", imageProcessing);

        return imageJson;
    }

    private String sendImage(JsonObject imageJson){
        HttpRequest videoRequest = context.getBean(HttpRequest.class);
        videoRequest.init(userData.getLogin(), userData.getPassword(), HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Image");

        return httpService.send(videoRequest, imageJson.toString());
    }

    @Override
    protected void preprocess() throws PreProcessException {

        // If there is no directory which should be created, then there is an error.
        if (!Files.isDirectory(Paths.get(descriptor.getProcessor().getOutputFile()))) {
            log.info("There is no directory. Video is being updated to status \"error\"");

            final String errorMessage = "Enter directory of images";
            log.error(errorMessage);
            throw new PreProcessException(errorMessage);
        }
    }

}
