package ru.hardware;

public class CPU {
    private String name;

    private Double maxMhz;

    private Integer coreCount;

    public CPU(String name, Double maxMhz, Integer coreCount){
        this.name = name;
        this.maxMhz = maxMhz;
        this.coreCount = coreCount;
    }

    public CPU(){

    }

    public Integer getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(Integer coreCount) {
        this.coreCount = coreCount;
    }

    public Double getMaxMhz() {
        return maxMhz;
    }

    public void setMaxMhz(Double maxMhz) {
        this.maxMhz = maxMhz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
