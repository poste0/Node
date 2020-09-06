package ru.descriptor;

import ru.data.UserData;
import ru.data.VideoData;
import ru.descriptor.dynamicParam.DynamicParamValueProvider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.FIELD)
public class Processor {
    @XmlElement
    private String command;

    @XmlElement
    private Params params;

    @XmlElement
    private String outputFile;

    @XmlAttribute
    private String type;

    @XmlElement
    private TextMessage textMessage;

    public String getCommand() {
        return command;
    }

    public String getExecLine(UserData userData, VideoData videoData) throws RuntimeException{
        StringBuilder execLineBuilder = new StringBuilder();
        execLineBuilder.append(this.command);
        params.getParams().forEach(param -> {
            if(param.isDynamic()){
                try {
                    setDynamicParamValue(param, userData, videoData);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            execLineBuilder.append(" ")
                            .append(params.getPrefix())
                            .append(param.getName())
                            .append(" ")
                            .append(param.getValue());
        });

        return execLineBuilder.toString();
    }


    private void setDynamicParamValue(Param param, UserData userData, VideoData videoData) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!param.isDynamic()){
            throw new IllegalArgumentException("Param in sot dynamic");
        }

        Class paramValueProvider = Class.forName(param.getDynamicValueProvider());

        List<Class> interfaces = Arrays.asList(paramValueProvider.getInterfaces());
        if(!interfaces.contains(DynamicParamValueProvider.class)){
            throw new NoClassDefFoundError();
        }

        DynamicParamValueProvider provider = (DynamicParamValueProvider) paramValueProvider.newInstance();

        param.setValue(provider.provide(userData, videoData));
    }


    public void setCommand(String command) {
        this.command = command;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TextMessage getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(TextMessage textMessage) {
        this.textMessage = textMessage;
    }
}
