package ru.hardware;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

@Component(value = "cpuParser")
public class CpuParser implements HardwareParser<CPU> {
    private static final String DELIMETER = ":";

    // keys from ubuntu's command line
    private static final String CORE_COUNT_KEY = "CPU(s)";

    private static final String MAX_MHZ_KEY = "CPU max MHz";

    private static final String NAME_KEY = "Model name";

    @Override
    public CPU parse(Stream<String> stream) {
        CPU cpu = new CPU();

        stream.forEach(line -> {
            String[] splittedLine = line.split(DELIMETER);
            if(splittedLine.length != 2){
                throw new IllegalArgumentException("Delimeter is wrong");
            }

            String key = splittedLine[0];
            String value = splittedLine[1].trim();

            switch (key){
                case CORE_COUNT_KEY:
                    cpu.setCoreCount(Integer.parseInt(value));
                    break;
                case MAX_MHZ_KEY:
                    value = value.replace(",", ".");
                    cpu.setMaxMhz(Double.parseDouble(value));
                    break;
                case NAME_KEY:
                    cpu.setName(value);
                    break;
            }
        });

        if(cpu.getName() == null || cpu.getMaxMhz() == null || cpu.getCoreCount() == null){
            throw new IllegalStateException("Cannot get cpu's information");
        }
        return cpu;
    }
}
