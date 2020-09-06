package ru.processor;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.opentest4j.TestAbortedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractProcessorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final String MESSAGE = "message";

    private List<File> contentFiles;

    private ProcessorMock processorMock;

    private List<File> initContentFiles(){
        return getFileList(
                Arrays.asList(
                        "someImage__!@$%.asdasasd.jpg",
                        "newImage.png"
                )
        );
    }

    @Before
    public void setUp(){
        try {
            processorMock = new ProcessorMock(temporaryFolder.getRoot().getName());
        } catch (FileNotFoundException e) {
            throw new TestAbortedException(e.getMessage());
        }
        contentFiles = initContentFiles();
    }

    @After
    public void tearDown(){
        temporaryFolder.delete();
    }

    @Test
    public void testGetMessageOkCase(){
        initContentFiles();

        List<File> textFiles = getFileList(
                Arrays.asList(
                        "someImage__!@$%.asdasasd.txt",
                        "newImage.txt"
                )
        );

        testEqualCase(textFiles);

        temporaryFolder.delete();
    }

    private void testEqualCase(List<File> files){
        for(int i = 0; i < contentFiles.size(); i++){
            File textFile = null;
            String contentFileName = contentFiles.get(i).getName().substring(0, contentFiles.get(i).getName().lastIndexOf("."));

            for(File file : files){
                String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                if(fileName.equals(contentFileName)){
                    textFile = file;
                    break;
                }
            }

            try {
                String message = processorMock.getMessage(contentFiles.get(i));

                Assert.assertEquals(message.trim(), (textFile.getName() + "\n" + MESSAGE).trim());
            } catch (FileExistsException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetMessageOtherFilesCase(){
        List<File> textFilesWithOtherFiles = getFileList(
                Arrays.asList(
                        "someImage__!@$%.asdasasd.txt",
                        "newImage.txt",
                        "image.txt",
                        "wrqwr.txt"
                )
        );

        testEqualCase(textFilesWithOtherFiles);
    }

    @Test
    public void testGetMessageWithDuplicateTest(){
        List<File> textFilesWithDuplicate = getFileList(
                Arrays.asList(
                        "someImage__!@$%.asdasasd.txt",
                        "newImage.txt",
                        "newImage.txtt"
                )
        );

        assertThrows(FileExistsException.class, () -> processorMock.getMessage(contentFiles.get(1)));
    }

    @Test
    public void testGetMessageWithoutFileCase(){
        List<File> textFilesWithoutFile = getFileList(
                Collections.singletonList(
                        "someImage__!@$%.asdasasd.txt"
                )
        );

        assertThrows(FileExistsException.class, () -> processorMock.getMessage(contentFiles.get(1)));
    }

    private List<File> getFileList(List<String> fileNames){
        return fileNames.stream().map(name -> {
            try{
                File file = temporaryFolder.newFile(name);
                FileUtils.writeLines(file, Arrays.asList(
                        name,
                        MESSAGE
                ));

                return file;
            }
            catch (IOException e){
                e.toString();
            }

            return null;
        }).collect(Collectors.toList());
    }

    @Test
    public void getDescriptor(){
        assertDoesNotThrow(() -> {processorMock.getDescriptor();});
    }
}