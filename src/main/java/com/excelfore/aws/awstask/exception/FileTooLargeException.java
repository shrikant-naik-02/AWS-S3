package com.excelfore.aws.awstask.exception;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }
}
