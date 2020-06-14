package ru.descriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Node {
    @XmlElement
    private Params params;

    @XmlElement
    private String server;

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
