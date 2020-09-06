package ru;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.data.UserData;
import ru.data.VideoData;
import ru.services.HardwareService;
import ru.services.ProcessorService;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

@RestController
public class MainController {

    @Autowired
    private ProcessorService service;

    @Autowired
    private HardwareService hardwareService;

    @Autowired
    private ApplicationContext context;

    @RequestMapping(value = "/file", method = RequestMethod.POST)
    public ResponseEntity get(@RequestBody MultipartFile file, @RequestParam(name = "login") String login,
                              @RequestParam(name = "password") String password, @RequestParam(name = "cameraId") String cameraId,
                                @RequestParam(name = "videoId") String videoId, @RequestParam(name = "nodeId") String nodeId){
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

                    UserData userData = createUserData(login, password);
                    VideoData videoData = createVideoData(videoId, cameraId, nodeId, result);

                    service.process(userData, videoData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return ResponseEntity.ok().build();
    }

    private UserData createUserData(String login, String password){
        UserData userData = context.getBean(UserData.class);
        userData.setLogin(login);
        userData.setPassword(password);

        return userData;
    }

    private VideoData createVideoData(String videoId, String cameraId, String nodeId, File result){
        VideoData videoData = context.getBean(VideoData.class);
        videoData.setVideoId(UUID.fromString(videoId));
        videoData.setCameraId(UUID.fromString(cameraId));
        videoData.setNodeId(UUID.fromString(nodeId));
        videoData.setVideoFile(result);

        return videoData;
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
