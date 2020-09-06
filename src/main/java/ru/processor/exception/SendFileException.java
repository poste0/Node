package ru.processor.exception;

public class SendFileException extends RuntimeException {
    public SendFileException(String message){
        super(message);
    }
}
