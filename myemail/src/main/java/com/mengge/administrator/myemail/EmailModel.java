package com.mengge.administrator.myemail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/17.
 *
 * 转换为可在主线程中更新的数据
 */
public class EmailModel {

    private String subject;
    private String from;
    private String sendTime;
    private String emailId;

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    //收件人
    private String toPerson;

    //获取抄送地址
    private String getCC;

    public String getToPerson() {
        return toPerson;
    }

    public void setToPerson(String toPerson) {
        this.toPerson = toPerson;
    }

    public String getGetCC() {
        return getCC;
    }

    public void setGetCC(String getCC) {
        this.getCC = getCC;
    }

    //附件数量
    private int attachmentsCount;
    /**
     * 收件箱总邮件数
     */
    private int totalMailCount;
    private List<AttachmentModel> attachmentModels = new ArrayList<>();
    //邮箱内容
    private String emailContent;

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }


    public List<AttachmentModel> getAttachmentModels() {
        return attachmentModels;
    }

    public void setAttachmentModels(List<AttachmentModel> attachmentModels) {
        this.attachmentModels = attachmentModels;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public void setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
    }

    public int getTotalMailCount() {
        return totalMailCount;
    }

    public void setTotalMailCount(int totalMailCount) {
        this.totalMailCount = totalMailCount;
    }
}
