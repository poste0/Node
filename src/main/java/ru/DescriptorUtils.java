package ru;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.descriptor.Descriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

public class DescriptorUtils {
    public static Descriptor getDescriptor(ResourceLoader loader) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Descriptor.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        File f = new File("descriptor.xml");

        FileUtils.copyInputStreamToFile(loader.getResource("classpath:descriptor.xml").getInputStream(), f);

        return (Descriptor) unmarshaller.unmarshal(f);
    }


}
