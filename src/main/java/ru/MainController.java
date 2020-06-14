package ru;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.services.HardwareService;
import ru.services.MainService;
import ru.services.ProcessorService;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.Executor;

@RestController
public class MainController {

    @Autowired
    private ProcessorService service;

    @Autowired
    private HardwareService hardwareService;

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public ResponseEntity get(@RequestBody MultipartFile file, @RequestParam(name = "login") String login,
                              @RequestParam(name = "password") String password, @RequestParam(name = "cameraId") String cameraId,
                                @RequestParam(name = "videoId") String videoId){
        String resultFileName = file.getOriginalFilename();
        if(Objects.isNull(resultFileName) || Strings.isEmpty(resultFileName)){
            return ResponseEntity.badRequest().build();
        }

        File result = new File(resultFileName);
        try {
            FileUtils.copyInputStreamToFile(file.getInputStream(), result);
        } catch (IOException e) {
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }

        Executor executor = new ConcurrentTaskExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    result.createNewFile();
                    service.process(result, login, password, cameraId, videoId);
                } catch (IOException | JAXBException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/cpu", method = RequestMethod.GET)
    public ResponseEntity<String> getCpu(){
        return new ResponseEntity(hardwareService.getCpuInformation(), HttpStatus.OK);
    }

    @RequestMapping(value = "/gpu", method = RequestMethod.GET)
    public ResponseEntity<String> getGpu(){
        return new ResponseEntity<>(hardwareService.getGPUInformation(), HttpStatus.OK);
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<String> getStatus(){
        return new ResponseEntity(service.isProcessing(), HttpStatus.OK);
    }
}
