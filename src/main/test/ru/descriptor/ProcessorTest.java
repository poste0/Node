package ru.descriptor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Collections;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorTest {
    private Descriptor testDescriptor;

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


    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void getExecLine() {
        Processor processor = testDescriptor.getProcessor();

        assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                processor.getExecLine(Collections.EMPTY_LIST);
            }
        });

        processor.getParams().getParams().forEach(param -> {
            if (param.getDynamic()){
                param.setValue("testValue Dynamic");
                param.setDynamic(false);
            }
        });

        String execLine = processor.getExecLine(Collections.EMPTY_LIST);

        assertEquals("test command --testParam1 testValue Dynamic --testParam2 value2", execLine);
        assertEquals("video.avi", processor.getOutputFile());
    }
}