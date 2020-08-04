package ru.services;

import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;


public interface ProcessorService {
    void process(File file, String login, String password, String cameraId, String videoId, String nodeId);

    boolean isProcessing();
}
