package ru;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import ru.descriptor.Descriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class);

    }
}
