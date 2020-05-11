package ru.hardware;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GpuParserTest {
    private List<String> gpuInformation;

    private HardwareParser<GPU> gpuParser;

    private static final String GPU_NAME = "Name of gpu in the system";

    private static final String MEMORY = "2048.5";

    @BeforeEach
    void setUp() {
        gpuInformation = new LinkedList<>();
        gpuInformation.add("some Parameter1: value1");
        gpuInformation.add("some Parameter2 :     value2");
        gpuInformation.add("Product Name: " + GPU_NAME);
        gpuInformation.add("FB Memory Usage");
        gpuInformation.add("Total: " + MEMORY);

        gpuParser = new GpuParser();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void parse() {
        Stream<String> gpuInformationStream = this.gpuInformation.stream();
        GPU gpu = gpuParser.parse(gpuInformationStream);
        String memoryFail = "wrong";

        assertEquals(gpu.getName(), GPU_NAME);
        assertEquals(gpu.getMemory(), Double.parseDouble(MEMORY));
        assertThrows(NumberFormatException.class, () -> {
           gpuInformation = gpuInformation.stream().map(line -> {
                if(line.contains(MEMORY)){
                    return  "Total: " + memoryFail;
                }
                return line;
            }).collect(Collectors.toList());
            Stream<String> wrongMemoryStreamGpu = gpuInformation.stream();

            GPU gpuWrongMemory = gpuParser.parse(wrongMemoryStreamGpu);
        });
    }
}