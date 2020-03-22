package ru;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class);

        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(Descriptor.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        ResourceLoader loader = applicationContext.getBean(ResourceLoader.class);
        File f = null;
        try {
            f = loader.getResource("classpath:descriptor.xml").getFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Descriptor descriptor = (Descriptor) unmarshaller.unmarshal(f);
            System.out.println(descriptor.getParams().get(0).getName());
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
}
