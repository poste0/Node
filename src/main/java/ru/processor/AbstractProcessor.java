package ru.processor;

import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.io.FileExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import ru.DescriptorUtils;
import ru.HttpRequest;
import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.Descriptor;
import ru.descriptor.TextMessage;
import ru.processor.exception.PreProcessException;
import ru.services.HttpService;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractProcessor implements Processor {
    protected UUID processingId;

    protected UserData userData;

    protected VideoData videoData;

    protected static final String READY_STATUS = "ready";

    protected static final String ERROR_STATUS = "error";

    protected Boolean isTextMessageUsed = null;

    /**
     * Loader to load descriptor from the resources
     */
    protected ResourceLoader loader;

    protected Descriptor descriptor;

    public AbstractProcessor(){}

    @Autowired
    public AbstractProcessor(UserData userData, VideoData videoData, ResourceLoader loader) throws FileNotFoundException {
        this.loader = loader;

        descriptor = getDescriptor();

        this.userData = userData;
        this.videoData = videoData;
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractProcessor.class);

    /**
     * Spring application context
     */
    @Autowired
    protected ApplicationContext context;

    /**
     * Http service to send messages to the platform
     */
    @Autowired
    protected HttpService httpService;

    @Override
    public void process(){
        try {
            preprocess();
            this.processingId = insertNewProcessing();
            insertNewData();
            updateSourceVideo(READY_STATUS);
        }
        catch (Exception e){
            updateSourceVideo(ERROR_STATUS);
            e.printStackTrace();
        }
    }

    protected abstract UUID insertNewProcessing();

    protected abstract void insertNewData();

    protected void updateSourceVideo(String status){
        log.info("Source video is updated to status {}", status);

        JsonObject updateVideo = new JsonObject();
        updateVideo.addProperty("status", status);

        HttpRequest videoUpdate = context.getBean(HttpRequest.class);
        videoUpdate.init(userData.getLogin(), userData.getPassword(), HttpMethod.PUT, MediaType.APPLICATION_JSON, "/app/rest/v2/entities/platform_Video/" + videoData.getVideoId().toString());
        String message = httpService.send(videoUpdate, updateVideo.toString());

        log.info("Source video has been updated to status {}", status);
    }

    protected abstract void preprocess() throws PreProcessException;

    public UUID getProcessingId() {
        return processingId;
    }

    public void setProcessingId(UUID processingId) {
        this.processingId = processingId;
    }

    public UserData getUserData(){return userData;}

    public void setUserData(UserData userData){this.userData = userData;}

    public VideoData getVideoData(){return videoData;}

    public void setVideoData(VideoData videoData){this.videoData = videoData;}

    protected Descriptor getDescriptor() throws FileNotFoundException {
        Descriptor descriptor = null;

        try {
            descriptor = DescriptorUtils.getDescriptor(loader);
        } catch (JAXBException | IOException e) {
            throw new FileNotFoundException("Descriptor is not found");
        }

        return descriptor;
    }

    protected Boolean isTextMessageUsed(){
        if(isTextMessageUsed == null) {
            isTextMessageUsed = descriptor.getProcessor().getTextMessage() != null;
        }

        return isTextMessageUsed;
    }

    protected String getMessage(File file) throws FileExistsException{
        TextMessage textMessage = descriptor.getProcessor().getTextMessage();

        File textMessageFile = findTextMessageFile(textMessage, file);
        StringBuilder messageBuilder = new StringBuilder();

        try {
            Files.readAllLines(textMessageFile.toPath()).forEach(line -> {
                messageBuilder.append(line).append("\n");
            });
            return messageBuilder.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return "";
    }

    private String getFileName(File file){
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
    }

    private String getFileExtension(File file){
        return file.getName().substring(file.getName().lastIndexOf("."));
    }

    private File findTextMessageFile(TextMessage textMessage, File file) throws FileExistsException{
        List<File> textMessageFiles = Arrays.asList(
                Objects.requireNonNull(
                        new File(textMessage.getOutputDirectory()).listFiles()
                )
        );

        String fileName = getFileName(file);
        textMessageFiles = textMessageFiles.stream().filter(fileStream -> getFileName(fileStream).equals(fileName) && !getFileExtension(fileStream).equals(getFileExtension(file))).collect(Collectors.toList());

        if(textMessageFiles.size() == 0){
            throw new FileExistsException("There is no file");
        }
        else if(textMessageFiles.size() > 1){
            throw new FileExistsException("There are more than 2 files");
        }

        return textMessageFiles.get(0);
    }
}
