package ru;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

public class ProcessOs {
    private Process process;

    private String command;

    private BufferedReader errorReader;

    private BufferedReader inputReader;

    private Integer status;

    public ProcessOs(String command){
        this.command = command;
        this.status = null;
    }

    public void startProcess(long period, Consumer<BufferedReader> errorConsumer, Consumer<BufferedReader> inputConsumer) throws IOException, InterruptedException {
        this.process = Runtime.getRuntime().exec(this.command);
        this.errorReader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
        this.inputReader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));

        onUpdate(period, errorConsumer, inputConsumer);

        this.status = process.waitFor();
    }

    private void onUpdate(long period, Consumer<BufferedReader> errorConsumer, Consumer<BufferedReader> inputConsumer){
        if(Objects.isNull(errorConsumer) || Objects.isNull(inputConsumer)){
            throw new IllegalArgumentException("Some consumer is null");
        }
        if(Objects.isNull(errorReader) || Objects.isNull(inputReader)){
            throw new IllegalStateException("Start the process then try again");
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                errorConsumer.accept(errorReader);
                inputConsumer.accept(inputReader);

                if(!Objects.isNull(status)){
                    timer.cancel();
                }
            }
        }, 0, period);

    }
}
