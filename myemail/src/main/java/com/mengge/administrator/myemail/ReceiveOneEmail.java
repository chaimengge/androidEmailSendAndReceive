package com.mengge.administrator.myemail;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.QPDecoderStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by chaimengge on 2016/8/9.
 */
public class ReceiveOneEmail {

    private MimeMessage msg = null;
    private String saveAttachmentsPath = "";
    //存放html类型的邮件内容
    private StringBuilder htmlBodyText = new StringBuilder();
    private String dateFormat = "yy-MM-dd HH:mm";
    //存放普通文本的邮件类型
    private StringBuilder plainBodyText = new StringBuilder();
    public ReceiveOneEmail(MimeMessage msg){
        this.msg = msg;
    }
    public void setMsg(MimeMessage msg) {
        this.msg = msg;
    }

    private List<AttachmentModel> attachmentModels = new ArrayList<>();
    /**
     * 获取发送邮件者信息
     * @return
     * @throws MessagingException
     */
    public String getFrom() {
        try {
            InternetAddress[] address = (InternetAddress[]) msg.getFrom();
            String from = address[0].getAddress();
            if(from == null){
                from = "";
            }
            String personal = address[0].getPersonal();
            if(personal == null){
                personal = "";
            }
            String fromaddr = personal +"<"+from+">";

            return  fromaddr;
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 获取邮件收件人，抄送，密送的地址和信息。根据所传递的参数不同 "to"-->收件人,"cc"-->抄送人地址,"bcc"-->密送地址
     * @param type
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public String getMailAddress(String type)  {
        String mailaddr = "";
        String addrType = type.toUpperCase();
        InternetAddress[] address = null;
        try {
            if(addrType.equals("TO")||addrType.equals("CC")||addrType.equals("BCC")){
                if(addrType.equals("TO")){
                    address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.TO);
                }
                if(addrType.equals("CC")){
                    address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.CC);
                }
                if(addrType.equals("BCC")){
                    address = (InternetAddress[]) msg.getRecipients(Message.RecipientType.BCC);
                }

                if(address != null){
                    for(int i = 0; i < address.length;i++){
                        String mail = address[i].getAddress();
                        if(mail == null){
                            mail = "";
                        }else{
                            mail = MimeUtility.decodeText(mail);
                        }
                        String personal = address[i].getPersonal();
                        if(personal == null){
                            personal = "";
                        }else{
                            personal = MimeUtility.decodeText(personal);
                        }
                        String compositeto = personal +"<"+mail+">";
                        mailaddr += ","+compositeto;
                    }
                    mailaddr = mailaddr.substring(1);
                }
            }else{
                throw new RuntimeException("Error email Type!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mailaddr;
    }

    /**
     * 获取邮件主题
     * @return
     *
     */
    public String getSubject(){
        String subject = "";

        try {
            subject = MimeUtility.decodeText(msg.getSubject());
            if(subject == null){
                subject = "";
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return subject;
    }

    /**
     * 获取邮件发送日期
     * @return
     * @throws MessagingException
     */
    public String getSendDate() {
        try {
            Date sendDate = msg.getSentDate();
            SimpleDateFormat smd = new SimpleDateFormat(dateFormat);
            return smd.format(sendDate);
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 获取邮件正文内容
     * @return
     */
    public String getHtmlBodyText(){

        return htmlBodyText.toString();
    }

    /**
     * 获取普通文本内容
     * @return
     */
    public String getPlainBodyText(){
        return  plainBodyText.toString();
    }

    /**
     * 解析邮件，将得到的邮件内容保存到一个stringBuffer对象中，解析邮件 主要根据MimeType的不同执行不同的操作，一步一步的解析
     * @param part
     * @throws MessagingException
     * @throws IOException
     */
    public void getMailContent(Part part) {
        try {
            String contentType = part.getContentType();
            int nameIndex = contentType.indexOf("name");
            boolean conname = false;
            if(nameIndex != -1){
                conname = true;
            }

            if(part.isMimeType("text/plain") && !conname){

                plainBodyText.append(decodeMailContent(part));

            }else if(part.isMimeType("text/html") && !conname){

                htmlBodyText.append(decodeMailContent(part));

            }else if(part.isMimeType("multipart/*")){
                DataSource source = new ByteArrayDataSource(part.getInputStream(), "multipart/*");
                Multipart multipart = new MimeMultipart(source);
                int count = multipart.getCount();
                for(int i = 0;i < count; i++){
                    getMailContent(multipart.getBodyPart(i));
                }
            }else if(part.isMimeType("message/rfc822")){
                getMailContent((Part) part.getContent());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String decodeMailContent(Part part){
        String content = "";
        try {
            if (contentIsQPStream(part)){
                content =  decodeQPContent((InputStream) part.getContent(), "UTF-8");
            }else if (contentIsBase64Stream(part)){
                content = decodeBase64Content((InputStream) part.getContent(), "UTF-8");
            }else if (is8BitStream(part))
            {
                content = decode8BitContent((InputStream) part.getContent(), "UTF-8");
            }else{
                content =(String) part.getContent();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return  content;
    }

    private boolean contentIsBase64Stream(Part part){
        try {
            return  part.getContent() instanceof BASE64DecoderStream;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }

    private boolean contentIsQPStream(Part part){
        try {
            return  part.getContent() instanceof QPDecoderStream;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }

    private boolean is8BitStream(Part part){
        try{
            return  part.getContent() instanceof InputStream;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断邮件是否需要回执，如需回执返回true，否则返回false
     * @return
     * @throws MessagingException
     */
    public boolean getReplySign(){
        boolean replySign = false;
        try {
            String needReply[] = msg.getHeader("Disposition-Notification-TO");
            if(needReply != null){
                replySign = true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return replySign;
    }

    /**
     * 获取此邮件的message-id
     * @return
     * @throws MessagingException
     */
    public String getMessageId(){
        try {
            return msg.getMessageID();
        }catch (Exception e){
            e.printStackTrace();
        }
      return  "";
    }

    /**
     * 判断此邮件是否已读，如果未读则返回false，已读返回true
     * @return
     * @throws MessagingException
     */
    public boolean isNew() {
        boolean isNew = false;
        try {
            Flags flags = msg.getFlags();
            Flags.Flag[] flag = flags.getSystemFlags();
            System.out.println("flags's length:"+flag.length);
            for(int i = 0;i < flag.length;i++){
                if(flag[i] == Flags.Flag.SEEN){
                    isNew = true;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isNew;
    }

    /**
     * 获得邮件的优先级
     * @param msg 邮件内容
     * @return 1(High):紧急  3:普通(Normal)  5:低(Low)
     * @throws MessagingException
     */
    public static String getPriority(MimeMessage msg) throws MessagingException {
        String priority = "普通";
        String[] headers = msg.getHeader("X-Priority");
        if (headers != null) {
            String headerPriority = headers[0];
            if (headerPriority.indexOf("1") != -1 || headerPriority.indexOf("High") != -1)
                priority = "紧急";
            else if (headerPriority.indexOf("5") != -1 || headerPriority.indexOf("Low") != -1)
                priority = "低";
            else
                priority = "普通";
        }
        return priority;
    }

    /**
     * 判断是是否包含附件
     * @param part
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public boolean isContainAttachments(Part part){
        boolean flag = false;
        try
        {
            String contentType = part.getContentType();
            if(part.isMimeType("multipart/*")){
                // Multipart multipart = (Multipart) part.getContent();
                DataSource source = new ByteArrayDataSource(part.getInputStream(), "multipart/*");
                Multipart multipart = new MimeMultipart(source);
                int count = multipart.getCount();
                for(int i = 0;i < count;i++){
                    BodyPart bodypart = multipart.getBodyPart(i);
                    String disPosition = bodypart.getDisposition();
                    if((disPosition != null)&&(disPosition.equals(Part.ATTACHMENT)||disPosition.equals(Part.INLINE))){
                        flag = true;
                    }else if(bodypart.isMimeType("multipart/*")){
                        flag = isContainAttachments(bodypart);
                    }else{
                        String conType = bodypart.getContentType();
                        if(conType.toLowerCase().indexOf("appliaction") != -1){
                            flag = true;
                        }
                        if(conType.toLowerCase().indexOf("name") != -1){
                            flag = true;
                        }
                    }
                    if (flag) break;
                }
            }else if(part.isMimeType("message/rfc822")){
                flag = isContainAttachments((Part) part.getContent());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }


    /**
     * 将附件文件名和流保存起来，用来下载或保存附件
     * @param part
     * @throws MessagingException
     * @throws IOException
     */
    public void saveAttachmentsToList(Part part) throws MessagingException, IOException{
        String fileName ;
        if(part.isMimeType("multipart/*")){
            DataSource source = new ByteArrayDataSource(part.getInputStream(), "multipart/*");
            Multipart mp = new MimeMultipart(source);
            for(int i = 0;i < mp.getCount(); i++){
                BodyPart bodyPart = mp.getBodyPart(i);
                String disposition = bodyPart.getDisposition();
                if((disposition != null)&&(disposition.equals(Part.ATTACHMENT)||disposition.equals(Part.INLINE))){
                    fileName = bodyPart.getFileName();
                    if(fileName != null){

                        if (fileName.toLowerCase().indexOf("gb2312") != -1){
                            fileName = MimeUtility.decodeText(fileName);
                        }

                        attachmentModels.add(new AttachmentModel(fileName, bodyPart.getInputStream()));
                    }

                }else if(bodyPart.isMimeType("multipart/*")){
                    saveAttachmentsToList(bodyPart);
                }else{
                    fileName = bodyPart.getFileName();
                    if(fileName != null && fileName.toLowerCase().indexOf("gb2312") != -1){
                        fileName = MimeUtility.decodeText(fileName);
                        attachmentModels.add(new AttachmentModel(fileName, bodyPart.getInputStream()));
                    }
                }
            }

        }else if(part.isMimeType("message/rfc822")){
            saveAttachmentsToList((Part) part.getContent());
        }
    }

    /**
     * 附件数量
     * @return
     */
    public int getAttachmentsCount(){
        return  attachmentModels.size();
    }

    public ArrayList<AttachmentModel> getAttachmentLists(){
        return (ArrayList<AttachmentModel>) attachmentModels;
    }
    /**
     * 获得保存附件的地址
     * @return
     */
    public String getSaveAttachmentsPath() {
        return saveAttachmentsPath;
    }
    /**
     * 设置保存附件地址
     * @param saveAttachmentsPath
     */
    public void setSaveAttachmentsPath(String saveAttachmentsPath) {
        this.saveAttachmentsPath = saveAttachmentsPath;
    }
    /**
     * 设置日期格式
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private String decodeBase64Content(InputStream in, String charset)
            {
        if (in == null) {
            return null;
        }
        try {
            //new BASE64DecoderStream(in)
            Reader  reader = new InputStreamReader(new BASE64DecoderStream(in), charset);
            return readContent(reader);
        } catch (Exception e) {
           e.printStackTrace();
        }
        return "";
    }

    private String decode8BitContent(InputStream in, String charset)
            throws MessagingException {
        if (in == null) {
            return null;
        }
        try {
            Reader reader = new InputStreamReader(in, charset);
            return readContent(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String decodeQPContent(InputStream in, String charset)
            throws MessagingException {
        if (in == null) {
            return null;
        }
        try {
            Reader  reader = new InputStreamReader(new QPDecoderStream(in), charset);
            return readContent(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String readContent(Reader reader) {
        BufferedReader br = null;
        StringBuilder sb = null;
        try {
            String line;
            br = new BufferedReader(reader);
            sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }
            // delete the last "\r\n"
            sb.delete(sb.length() - 2, sb.length());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sb != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  "";
    }

    public static void main(String[] args) throws MessagingException, IOException {
        try {
            // 准备连接服务器的会话信息
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "pop3");       // 协议
            props.setProperty("mail.pop3.port", "110");             // 端口
            props.setProperty("mail.pop3.host", "mail.ultrapower.com.cn");    // pop3服务器

            // 创建Session实例对象
            Session session = Session.getInstance(props);
            Store store = session.getStore("pop3");
            store.connect("chaimengge@ultrapower.com.cn", "chai?59560");
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message msgs[] = folder.getMessages(1, 1);
            int count = msgs.length;
            System.out.println("Message Count:"+count);
            ReceiveOneEmail rm = null;
            for(int i = 0;i < count;i++){
                rm = new ReceiveOneEmail((MimeMessage) msgs[i]);
            }
            folder.close(true);
            store.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
