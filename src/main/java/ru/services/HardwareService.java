package ru.services;

import org.springframework.stereotype.Service;


public interface HardwareService {
    String getCpuInformation();

    String getGPUInformation();
}
