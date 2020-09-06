package ru.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.Descriptor;
import ru.descriptor.Param;
import ru.processor.ImageProcessor;
import ru.processor.Processor;
import ru.processor.VideoProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

@Service
public class ProcessorServiceImpl implements ProcessorService {

    /**
     * Loader to load descriptor from the resources
     */
    @Autowired
    private ResourceLoader loader;

    /**
     * Consumer which is passed to
     * @see ProcessOs
     */
    private final Consumer<BufferedReader> readerConsumer = reader -> {reader.lines().forEach(System.out::println);};

    /**
     * Http service to send messages to the platform
     */
    @Autowired
    private HttpService httpService;

    /**
     * Spring application context
     */
    @Autowired
    private ApplicationContext context;

    /**
     * A flag which shows if the node is processing a video
     * todo It doesn't work yet. Need to do something.
     */
    private boolean isProcessing = false;

    /**
     * A list with dynamic params which are calculated at runtime
     */
    private List<Param> dynamicParams;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ProcessorServiceImpl.class);

    /**
     * Processes a video got from the platform. Sends the processing result back.
     * @param userData
     * @param videoData
     */

    @Override
    public void process(UserData userData, VideoData videoData){
        try {
            Descriptor descriptor = DescriptorUtils.getDescriptor(loader);

            String processingType = descriptor.getProcessor().getType();
            log.info("Processing for user {}, video with id {} with type {} has started", userData.getLogin(), videoData.getVideoId().toString(), processingType);

            if (!videoData.getVideoFile().exists()) {
                log.info("File doesn't exist. Creating a new file");
                boolean isFileCreated = videoData.getVideoFile().createNewFile();
                log.info("File has been created");
                if (!isFileCreated) {
                    log.error("File with name {} has not been created", videoData.getVideoFile().getName());
                    throw new FileNotFoundException("The file has not been created");
                }
            }

            //todo It's a dynamic param. It doesn't work well. Need to fix it
            Param fileParam = new Param();
            fileParam.setName("file");
            fileParam.setValue(videoData.getVideoFile().getPath());

            // If processing results are images and there is no directory yet, then the directory must be created
            if (!Files.isDirectory(Paths.get(descriptor.getProcessor().getOutputFile())) && descriptor.getProcessor().getType().equals("Image")) {
                log.info("There is no directory for result images. Creating the directory");
                Files.createDirectory(Paths.get(descriptor.getProcessor().getOutputFile()));
            }

            ProcessOs videoProcess = new ProcessOs(descriptor.getProcessor().getExecLine(userData, videoData));
            long period = 1000;
            try {
                log.info("Process with command {} has started", videoProcess.getCommand());
                videoProcess.startProcess(period, readerConsumer, readerConsumer);

                Files.delete(videoData.getVideoFile().toPath());
                log.info("Process with command {} has finished. Video file has been deleted", videoProcess.getCommand());


            } catch (InterruptedException e) {
                log.error("Process with command {} has finished with an error. Updating the source video to status \"error\"", videoProcess.getCommand());
                updateSourceVideo(userData.getLogin(), userData.getPassword(), videoData.getVideoId().toString(), "error");
                throw new InterruptedException("Processing of video failed");
            }

            Processor processor = null;
            if (processingType.equals("Video")) {
                processor = context.getBean(VideoProcessor.class, userData, videoData);
            } else if (descriptor.getProcessor().getType().equals("Image")) {
                processor = context.getBean(ImageProcessor.class, userData, videoData);
            } else {
                log.error("Wrong type of processor");
                throw new IllegalArgumentException("Wrong type of processor");
            }
            processor.process();
        }
        catch (Exception e){
            // todo It must be fixed because there is no information about the error.
            log.error("Some error");
            e.printStackTrace();
        }
    }

    /**
     * Sends result images to the platform.
     * @param login User's login
     * @param password User's password
     * @param videoId Video's id
     * @param directory Directory in which image files are stored
     * @param imageProcessingId image processing id
     */
    private void insertNewImages(String login, String password, String videoId, String directory, UUID imageProcessingId) {
        log.info("Images are sent to the platform");

        File files = new File(directory);
        File[] images = files.listFiles();

        for(File image: images){
            LinkedMultiValueMap<String, Object> imageBody = new LinkedMultiValueMap<>();
            FileSystemResource fileResource = new FileSystemResource(image);
            imageBody.add("file", fileResource);

            HttpRequest request = context.getBean(HttpRequest.class);
            request.init(login, password, HttpMethod.POST, MediaType.MULTIPART_FORM_DATA, "/app/rest/v2/files?name=" + image.getPath());

            String imageStatus = httpService.send(request, imageBody);

            JsonObject imageJson = new JsonObject();
            imageJson.addProperty("name", image.getName());

            JsonObject fileDescriptor = new JsonObject();
            String fileDescriptorId = JsonParser.parseString(imageStatus).getAsJsonObject().get("id").getAsString();
            fileDescriptor.addProperty("id", fileDescriptorId);

            JsonObject parentVideo = new JsonObject();
            parentVideo.addProperty("id", videoId);

            JsonObject imageProcessing = new JsonObject();
            imageProcessing.addProperty("id", imageProcessingId.toString());

            imageJson.add("fileDescriptor", fileDescriptor);
            imageJson.add("parentVideo", parentVideo);
            imageJson.add("imageProcessing", imageProcessing);

            HttpRequest videoRequest = context.getBean(HttpRequest.class);
            videoRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Image");
            String message = httpService.send(videoRequest, imageJson.toString());

            log.info("Image of image processing with id {} has sent to the platform", imageProcessingId.toString());
        }

    }

    /**
     * Creates and sends a image processing object to the platform.
     * @param login User's login
     * @param password User's password
     * @param nodeId Node's id
     * @return Id of the image processing object which received from the platform
     */
    private UUID insertNewImageProcessings(String login, String password, String nodeId){
        log.info("Image processing is sent to the platform");

        JsonObject imageProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", nodeId);

        imageProcessing.add("node", node);

        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_ImageProcessing");
        String message = httpService.send(videoProcessingRequest, imageProcessing.toString());

        final String imageProcessingId = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();
        log.info("Image processing with id {} has been created", imageProcessingId);

        return UUID.fromString(imageProcessingId);
    }

    /**
     * Updates the source video in the platform to READY status if the processing is ok. Otherwise ERROR.
     * @param login User's login
     * @param password USer's password
     * @param videoId Video's id
     * @param status Status of the processing
     */
    private void updateSourceVideo(String login, String password, String videoId, String status){
        log.info("Source video is updated to status {}", status);

        JsonObject updateVideo = new JsonObject();
        updateVideo.addProperty("status", status);

        HttpRequest videoUpdate = context.getBean(HttpRequest.class);
        videoUpdate.init(login, password, HttpMethod.PUT, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video/" + videoId);
        String message = httpService.send(videoUpdate, updateVideo.toString());

        log.info("Source video has been updated to status {}", status);

    }

    /**
     * Sends video result back
     * @param login User's login
     * @param password User's password
     * @param videoId Video's id
     * @param cameraId Camera's id
     * @param file File with the processed video
     * @param videoFileInfo Info about the video file got from the platform
     */
    private void insertNewVideo(String login, String password, String videoId, String cameraId, File file, String videoFileInfo, UUID videoProcessingId){
        log.info("Result video is sent to the platform");

        JsonObject video = new JsonObject();
        video.addProperty("id", videoId);

        JsonObject camera = new JsonObject();
        camera.addProperty("id", cameraId);
        String fileDescriptorId = JsonParser.parseString(videoFileInfo).getAsJsonObject().get("id").getAsString();

        JsonObject fileDescriptor = new JsonObject();
        fileDescriptor.addProperty("id", fileDescriptorId);

        JsonObject videoProcessing = new JsonObject();
        videoProcessing.addProperty("id", videoProcessingId.toString());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "processed " + file.getName());
        jsonObject.add("camera", camera);
        jsonObject.addProperty("parentName", file.getName());
        jsonObject.add("fileDescriptor", fileDescriptor);
        jsonObject.add("parentVideo", video);
        jsonObject.addProperty("status", "processed");
        jsonObject.add("videoProcessing", videoProcessing);

        HttpRequest videoRequest = context.getBean(HttpRequest.class);
        videoRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video");
        String message = httpService.send(videoRequest, jsonObject.toString());

        log.info("Video of video processing with id {} has sent to the platform", videoProcessingId.toString());
    }

    /**
     *  @param login
     * @param password
     * @param videoId
     * @param nodeId
     * @return
     */
    private UUID insertNewVideoProcessing(String login, String password, String videoId, String nodeId){
        log.info("Video processing is sent to the platform");

        JsonObject videoProcessing = new JsonObject();

        JsonObject node = new JsonObject();
        node.addProperty("id", nodeId);

        videoProcessing.add("node", node);

        HttpRequest videoProcessingRequest = context.getBean(HttpRequest.class);
        videoProcessingRequest.init(login, password, HttpMethod.POST, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_VideoProcessing");
        String message = httpService.send(videoProcessingRequest, videoProcessing.toString());

        final String videoProcessingId = JsonParser.parseString(message).getAsJsonObject().get("id").getAsString();;
        log.info("Image processing with id {} has been created", videoProcessingId);


        return UUID.fromString(videoProcessingId);
    }

    @Override
    public boolean isProcessing() {
        return isProcessing;
    }

}
