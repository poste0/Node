package ru.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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

    public String getCommand() {
        return command;
    }

    public String getExecLine(List<Param> dynamicParams){
        StringBuilder execLineBuilder = new StringBuilder();
        execLineBuilder.append(this.command);
        params.getParams().forEach(param -> {
            if(param.getDynamic()){
                if(!dynamicParams.stream().map((Function<Param, Object>) Param::getName).collect(Collectors.toList()).contains(param.getName())) {
                    throw new IllegalStateException("Execution line has dynamic empty parameter");
                }

                Param finalParam = param;
                param = dynamicParams.stream().filter(param1 -> param1.getName().equals(finalParam.getName())).findAny().orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        return new RuntimeException("No dynamic param in the list");
                    }
                });
            }

            execLineBuilder.append(" ")
                            .append(params.getPrefix())
                            .append(param.getName())
                            .append(" ")
                            .append(param.getValue());
        });

        return execLineBuilder.toString();
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
}
