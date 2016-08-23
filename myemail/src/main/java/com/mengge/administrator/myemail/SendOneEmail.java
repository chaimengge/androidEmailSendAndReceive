package com.mengge.administrator.myemail;

import com.mengge.administrator.utils.EmailUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by Administrator on 2016/8/11.
 *
 * 发送一封邮件实体
 */
public class SendOneEmail {
    //主题
    private String subject;

    //内容

    private String emailContent;

    //附件

    private List<File> attachMentsList =  new ArrayList<>();

    //接收人
    private String receivePerson;

    //多个收件人
    private ArrayList<String> receivePersons;

    //发件人
    private String sendPerson = "chaimengge@ultrapower.com.cn";

    //抄送
    private ArrayList <String> CCLists;

    //密送
    private ArrayList <String> BCCLists;


    public SendOneEmail(){
        configSendMail();
    }

    public String getSendPerson() {
        return sendPerson;
    }

    public void setSendPerson(String sendPerson) {
        this.sendPerson = sendPerson;
    }

    /**
     * 添加抄送邮件地址
     * @param ccEmailAdd
     */
    public void addCC(String ccEmailAdd){
        if (CCLists == null){
            CCLists = new ArrayList<>();
        }

        if (EmailUtil.isEmail(ccEmailAdd)){
            CCLists.add(ccEmailAdd);
        }
    }

    //增加多个收件人
    public void addReceivePerson(String receivePersonEmail){
            if (receivePersons == null){
                receivePersons = new ArrayList<>();
            }

            if (EmailUtil.isEmail(receivePersonEmail)){
                receivePersons.add(receivePersonEmail);
            }
    }

    public void addBCCEmailAdd(String bccEmailAdd){
        if (BCCLists == null){
            BCCLists = new ArrayList<>();
        }

        if (EmailUtil.isEmail(bccEmailAdd)){
            BCCLists.add(bccEmailAdd);
        }
    }

    public void addAttachment(File file){
        if (file != null && file.isFile()){
            attachMentsList.add(file);
        }
    }

    public String getSubject() {
        return subject  == null?"":subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEmailContent() {
        return emailContent == null?"":emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }

    public List<File> getAttachMentsList() {
        return attachMentsList;
    }


    public String getReceivePerson() {
        return receivePerson;
    }

    public void setReceivePerson(String receivePerson) {
        this.receivePerson = receivePerson;
    }

    public ArrayList<String> getCCLists() {
        return CCLists;
    }

    public void setCCLists(ArrayList<String> CCLists) {
        this.CCLists = CCLists;
    }

    public ArrayList<String> getBCCLists() {
        return BCCLists;
    }

    public void setBCCLists(ArrayList<String> BCCLists) {
        this.BCCLists = BCCLists;
    }

    /**
     * 创建只有文本消息的邮件
     * @param session
     * @return
     */
    public MimeMessage createTextEmail (Session session) throws Exception {
        MimeMessage message = new MimeMessage(session);
            //设置主题
            message.setSubject(getSubject());
            //设置邮件内容
            message.setContent(getEmailContent(), "text/html;charset=UTF-8");
            //发送日期
            message.setSentDate(new Date());
            //设置发件人
            message.setFrom(new InternetAddress(getSendPerson()));
            //添加收件人
            addMutiReceivePerson(message);
            //添加抄送
            setCC(message);
            //添加密送
            setBCC(message);
            message.saveChanges();
        return  message;
    }

    /**
     * 创建混合邮件， 包含正文和附件
     * @param session
     * @return
     */
    public MimeMessage createMixEmail(Session session) throws Exception{
        MimeMessage mimeMessage = new MimeMessage(session);
            //设置邮件主题
            mimeMessage.setSubject(getSubject());
            //设置发件人
            mimeMessage.setFrom(new InternetAddress(getSendPerson()));
            // 设置收件人 一个或多个
            addMutiReceivePerson(mimeMessage);
            //添加抄送
            setCC(mimeMessage);
            //添加密送
            setBCC(mimeMessage);
            // 设置发送时间
            mimeMessage.setSentDate(new Date());

            //添加正文和附件
            attachAttachments(mimeMessage);
            mimeMessage.saveChanges();
            return  mimeMessage;
    }

    /**
     * 加上附件
     */
    private void attachAttachments(MimeMessage message){
        // 创建邮件附件
        MimeMultipart mimeMultipart = new MimeMultipart("mixed");
        try {
            if (attachMentsList != null && attachMentsList.size() > 0) {

                for (File attachment:attachMentsList) {
                    MimeBodyPart attach = new MimeBodyPart();
                    DataHandler dh = new DataHandler(new FileDataSource(attachment));
                    attach.setDataHandler(dh);
                    attach.setFileName(MimeUtility.encodeText(dh.getName(), "gb2312", null));
                    mimeMultipart.addBodyPart(attach);
                }
            }
            //映射邮件正文
            MimeBodyPart textPart = new MimeBodyPart();

            textPart.setContent(getEmailContent(), "text/html;charset=utf-8");

            mimeMultipart.addBodyPart(textPart);

            message.setContent(mimeMultipart);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 添加多个收件人
     */
    private void addMutiReceivePerson(MimeMessage message){
        try{
            if (receivePersons != null && receivePersons.size() > 0) {

                InternetAddress[] addresses = new InternetAddress[receivePersons.size()];

                for (int i = 0; i < receivePersons.size(); i++) {
                    String emailAdd = receivePersons.get(i);

                    String name = emailAdd.substring(0 , emailAdd.indexOf("@"));
                    addresses [i] = new  InternetAddress(receivePersons.get(i), name, "UTF-8");
                }
                message.setRecipients(MimeMessage.RecipientType.TO, addresses);

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *向邮件服务器添加抄送
     */
    private void setCC(MimeMessage message){
        try
        {
            if (CCLists != null && CCLists.size() > 0)
            {
                for (int i = 0; i < CCLists.size(); i++) {
                    message.setRecipient(Message.RecipientType.CC, new InternetAddress(CCLists.get(i)));
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 向邮件服务器添加密送
     */
    private void setBCC(MimeMessage message){
        try
        {
            if (BCCLists != null && BCCLists.size() > 0)
            {
                for (int i = 0; i < BCCLists.size(); i++)
                    message.setRecipient(Message.RecipientType.BCC, new InternetAddress(BCCLists.get(i)));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 不做如下配置，有可能在发送邮件时报异常
     */
    private void configSendMail(){
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }
}
