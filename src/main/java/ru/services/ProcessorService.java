package ru.services;

import org.springframework.stereotype.Service;
import ru.data.UserData;
import ru.data.VideoData;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;


public interface ProcessorService {
    void process(UserData userData, VideoData videoData);

    boolean isProcessing();
}
