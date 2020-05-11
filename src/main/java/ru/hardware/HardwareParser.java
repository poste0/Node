package ru.hardware;

import ru.ProcessOs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public interface HardwareParser<T> {
    T parse(Stream<String> stream);

    default T parse(String processName) throws IOException, InterruptedException {
        ProcessOs process = new ProcessOs(processName);
        final BufferedReader[] bufferedReader = new BufferedReader[1];
        process.startProcess(1000, errorReader -> {

        }, processReader -> {
            bufferedReader[0] = processReader;
        });
        Stream<String> readerStream = bufferedReader[0].lines();

        return parse(readerStream);
    }
}
