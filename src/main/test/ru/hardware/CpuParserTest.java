package ru.hardware;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CpuParserTest {
    private List<String> cpuInformation;

    private HardwareParser<CPU> parser;

    private static final String CPU_NAME = "Cpu";

    private static final String CORE_COUNTS = "4";

    private static final String MAX_MHZ = "5.0";

    @BeforeEach
    void setUp() {
        cpuInformation = new LinkedList<>();
        cpuInformation.add("some Parameter1: value1");
        cpuInformation.add("some Parameter2:      value2");
        cpuInformation.add("CPU(s): " + CORE_COUNTS);
        cpuInformation.add("CPU max MHz: " + MAX_MHZ);
        cpuInformation.add("Model name: " + CPU_NAME);

        parser = new CpuParser();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void parse() {
        Stream<String> cpuInformationStream = cpuInformation.stream();
        CPU cpu = parser.parse(cpuInformationStream);
        String coreCountsWrong = "wrong";
        String maxMhzWrong = "wrong";

        assertEquals(cpu.getName(), CPU_NAME);
        assertEquals(cpu.getCoreCount(), Integer.parseInt(CORE_COUNTS));
        assertEquals(cpu.getMaxMhz(), Double.parseDouble(MAX_MHZ));

        assertThrows(NumberFormatException.class, () -> {
            cpuInformation = cpuInformation.stream().map(line -> {
                if(line.contains(MAX_MHZ)){
                    return "CPU max MHz: " + maxMhzWrong;
                }
                else if(line.contains(CORE_COUNTS)){
                    return "CPU(s): " + coreCountsWrong;
                }
                return line;
            }).collect(Collectors.toList());

            Stream<String> cpuInformationWrong = cpuInformation.stream();
            CPU cpuWrong = parser.parse(cpuInformationWrong);
        });
    }
}