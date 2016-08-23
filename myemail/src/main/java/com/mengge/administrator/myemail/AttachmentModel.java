package com.mengge.administrator.myemail;

import java.io.InputStream;

/**
 * Created by Administrator on 2016/8/17.
 */
public class AttachmentModel{

    private String fileName;
    private InputStream inputStream;

    private int attachmentsLength;

    public int getAttachmentsLength() {
        return attachmentsLength;
    }

    public void setAttachmentsLength(int attachmentsLength) {
        this.attachmentsLength = attachmentsLength;
    }

    public AttachmentModel(String fileName, int attachmentsLength, InputStream inputStream){
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.attachmentsLength = attachmentsLength;

    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
