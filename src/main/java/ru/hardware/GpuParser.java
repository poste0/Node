package ru.hardware;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(value = "gpuParser")
public class GpuParser implements HardwareParser<GPU> {
    private static final String DELIMETER = ":";

    private static final String NAME_KEY = "Product Name";

    private static final String MEMORY_KEY = "FB Memory Usage";

    @Override
    public GPU parse(Stream<String> stream) {
        GPU gpu = new GPU();

        List<String> gpuInfo = stream.collect(Collectors.toList());
        for(int i = 0; i < gpuInfo.size(); i++){
            if(gpuInfo.get(i).trim().equals(MEMORY_KEY)){
                String value= "";
                if(i < gpuInfo.size() - 1){
                    value = gpuInfo.get(i + 1).split(DELIMETER)[1].trim().split(" ")[0];
                }
                gpu.setMemory(Double.parseDouble(value));

                continue;
            }

            String[] splittedLine = gpuInfo.get(i).split(DELIMETER);
            if (splittedLine.length != 2){
                continue;
            }

            String key = splittedLine[0].trim();
            String value = splittedLine[1].trim();

            if (NAME_KEY.equals(key)) {
                gpu.setName(value);
            }
        }

        if(gpu.getName() == null || gpu.getMemory() == null){
            throw new IllegalStateException("Cannot get gpu's information");
        }
        return gpu;
    }
}
