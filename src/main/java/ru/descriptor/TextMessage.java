package ru.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents textMessage tag from descriptor.xml. If it is used, then a message is added for result content.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TextMessage {
    /**
     * Shows the directory in which text files are stored
     */
    @XmlElement
    private String outputDirectory;

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
