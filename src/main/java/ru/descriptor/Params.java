package ru.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Params {
    @XmlAttribute(required = true)
    private String prefix;

    @XmlElement(name = "param")
    private List<Param> params;

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
