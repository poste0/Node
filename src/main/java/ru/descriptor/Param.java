package ru.descriptor;

import ru.descriptor.dynamicParam.DynamicParamValueProvider;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Arrays;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Param {
    @XmlElement
    private String name;

    @XmlElement
    private String value;

    @XmlAttribute
    private Boolean isDynamic;

    @XmlElement
    private String dynamicValueProvider;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(Boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getDynamicValueProvider() {
        return dynamicValueProvider;
    }

    public void setDynamicValueProvider(String dynamicValueProvider) {
        this.dynamicValueProvider = dynamicValueProvider;
    }
}
