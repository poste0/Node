package ru.data;

import org.springframework.stereotype.Component;

import java.io.File;
import java.text.ParseException;
import java.util.UUID;

@Component
public class VideoData {
    private UUID videoId;

    private UUID cameraId;

    private UUID nodeId;

    private File videoFile;

    public VideoData(UUID videoId, UUID cameraId, UUID nodeId, File videoFile){
        this.videoId = videoId;
        this.cameraId = cameraId;
        this.nodeId = nodeId;
        this.videoFile = videoFile;
    }

    public VideoData(){}

    public VideoData(String videoId, String cameraId, String nodeId, File videoFile) throws IllegalArgumentException {
        this(UUID.fromString(videoId), UUID.fromString(cameraId), UUID.fromString(nodeId), videoFile);
    }

    public UUID getVideoId() {
        return videoId;
    }

    public void setVideoId(UUID videoId) {
        this.videoId = videoId;
    }

    public UUID getCameraId() {
        return cameraId;
    }

    public void setCameraId(UUID cameraId) {
        this.cameraId = cameraId;
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }
}
