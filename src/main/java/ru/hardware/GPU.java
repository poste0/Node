package ru.hardware;

public class GPU {
    private String name;

    private Double memory;

    public GPU(String name, Double memory){
        this.name = name;
        this.memory = memory;
    }

    public GPU(){}

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
