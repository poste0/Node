package ru;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Descriptor {

    @XmlElement
    private String pathToCommand;

    @XmlElement
    private String command;

    @XmlElement
    private List<Param> param;

    public String getPathToCommand() {
        return pathToCommand;
    }

    public void setPathToCommand(String pathToCommand) {
        this.pathToCommand = pathToCommand;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<Param> getParams() {
        return param;
    }

    public void setParams(List<Param> params) {
        this.param = params;
    }
}
