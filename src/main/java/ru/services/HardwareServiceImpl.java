package ru.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.hardware.CPU;
import ru.hardware.GPU;
import ru.hardware.HardwareParser;

import java.io.IOException;

@Service
public class HardwareServiceImpl implements HardwareService {
    @Autowired
    @Qualifier("cpuParser")
    private HardwareParser<CPU> cpuParser;

    @Autowired
    @Qualifier("gpuParser")
    private HardwareParser<GPU> gpuParser;

    private static final String CPU_COMMAND = "lscpu";

    private static final String GPU_COMMAND = "nvidia-smi -q";

    @Override
    public String getCpuInformation() {
        try {
            CPU cpu = cpuParser.parse(CPU_COMMAND);

            return cpu.getName();
        } catch (IOException | InterruptedException e) {
            return "Cannot get CPU's information";
        }
    }

    @Override
    public String getGPUInformation() {
        try {
            GPU gpu = gpuParser.parse(GPU_COMMAND);

            return gpu.getName();
        } catch (IOException | InterruptedException e) {
            return "Cannot get GPU's information";
        }
    }
}
