package ru.processor;

import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.Descriptor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class ProcessorMock extends AbstractProcessor {
    private String jUnitFolder;

    public ProcessorMock(UserData userData, VideoData videoData) throws FileNotFoundException {
        this.userData = userData;
        this.videoData = videoData;
        this.descriptor = getDescriptor();
    }

    public ProcessorMock(String jUnitFolder) throws FileNotFoundException{
        this.jUnitFolder = jUnitFolder;

        this.descriptor = getDescriptor();
    }

    @Override
    protected UUID insertNewProcessing() {
        throw new NotImplementedException();
    }

    @Override
    protected void insertNewData() {
        throw new NotImplementedException();
    }

    @Override
    protected void preprocess() {
        throw new NotImplementedException();
    }

    @Override
    protected Descriptor getDescriptor() throws FileNotFoundException {
        try {
            JAXBContext context  = JAXBContext.newInstance(Descriptor.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            File f = new File("src/main/test/ru/descriptorTest.xml");

            Descriptor descriptor = (Descriptor) unmarshaller.unmarshal(f);
            descriptor.getProcessor().getTextMessage().setOutputDirectory("/tmp/" + jUnitFolder);

            return descriptor;
        } catch (JAXBException e) {
            throw new FileNotFoundException("Descriptor is not found");
        }
    }
}
