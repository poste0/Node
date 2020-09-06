package ru.descriptor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import ru.data.UserData;
import ru.data.VideoData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {
    private Descriptor testDescriptor;

    private UserData userData;

    private VideoData videoData;

    private static final String FILE_NAME = "Videofile.mp4";

    @BeforeEach
    void setUp() {
        try {
            JAXBContext context  = JAXBContext.newInstance(Descriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File f = new File("src/main/test/ru/descriptorTest.xml");

            this.testDescriptor = (Descriptor) unmarshaller.unmarshal(f);
        } catch (JAXBException e) {
            e.printStackTrace();
            assertEquals(0, -1);
        }

        userData = new UserData("login", "password");
        videoData = new VideoData(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new File(FILE_NAME));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void getExecLine() {
        Processor processor = testDescriptor.getProcessor();

        Param dynamicParam = processor.getParams().getParams().stream().filter(param -> param.getName().equals("testParam1")).findFirst().get();
        final String valueProvider = dynamicParam.getDynamicValueProvider();
        dynamicParam.setDynamicValueProvider("");

        assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                processor.getExecLine(userData, videoData);
            }
        });

        dynamicParam.setDynamicValueProvider(valueProvider);

        String execLine = processor.getExecLine(userData, videoData);

        assertEquals("test command --testParam1 " + FILE_NAME + " --testParam2 value2", execLine);
        assertEquals("video.avi", processor.getOutputFile());
    }
}