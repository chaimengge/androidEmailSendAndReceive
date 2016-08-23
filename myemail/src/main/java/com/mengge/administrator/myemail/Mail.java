package com.mengge.administrator.myemail;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * Created by Administrator on 2016/8/11.
 * 控制邮件的发送和接收, 发送邮件，必须实例化{@link SendOneEmail}, 初始化发送邮件的相关内容,附件或正文
 */
public  class Mail{

    private SendOneEmail mSendOneEmail;
    private MailConfig.emailType mMailType;

    public ReceiveEmailListener mReceiveEmailListener;

    public SendEmailListener mSendEmailListener;

    private Store mStore;
    private Folder mFolder;
    private Transport mTransport;

    private Context mContext;
    private MailTask mMailTask;

    private String mLoadingText = "加载中...";
    public Mail(){

    }
    public Mail(Context c){
        mContext = c;
    }

    public Mail(Context c, SendOneEmail oneEmail){
        mContext = c;
        mSendOneEmail = oneEmail;
    }

    public interface  ReceiveEmailListener{
        void receiveMailComplete(ArrayList<Object> dataOrException);
    }

    public void setOnReceiveEmailListener(ReceiveEmailListener listener){
        mReceiveEmailListener = listener;
    }

    public interface  SendEmailListener{
        void onSendEmailComplete(ArrayList<Object> dataOrException);
    }
    public void setOnSendEmailListener(SendEmailListener listener){
        mSendEmailListener = listener;
    }

    /**
     * 提供改变加载文字的选项，默认是加载中...
     * @param loadingText
     */
    public void setLoadingText(String loadingText){
        mLoadingText = loadingText == null ? mLoadingText :loadingText;
    }

    public void setMailType(MailConfig.emailType type){
        mMailType = type;
    }


    public MailConfig.emailType getMailType(){
        return mMailType;
    }
    /**
     * 发送邮件
     * @param emailType  可以是Null 默认是不带附件的普通邮件, 可选
     */
    public void sendMail(MailConfig.emailType emailType){
        mMailType = emailType;
        if (mMailTask == null){
            mMailTask = new MailTask();
        }
        mMailTask.execute(false, -1, -1);
    }


    public void setContext(Context c){
        mContext = c;
    }

    public void setSendOneMail(SendOneEmail oneMail){
        mSendOneEmail = oneMail;
    }

    private ArrayList<Object> send(){
        ArrayList<Object> exceptionList = new ArrayList<>();
        try {
            Properties prop = System.getProperties();
            prop.setProperty("mail.smtp.host", MailConfig.SEND_SERVER);
            prop.setProperty("mail.transport.protocol", MailConfig.SMTP);
            prop.setProperty("mail.smtp.auth", "true");
            prop.setProperty("mail.smtp.port", "25");
            //使用JavaMail发送邮件的5个步骤
            //1、创建session
            Session session = Session.getInstance(prop, new EmailAuthenticator());
            //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
            //session.setDebug(false);
            //2、通过session得到transport对象
            mTransport = session.getTransport();
            //3、连上邮件服务器
            mTransport.connect();
            //4、创建邮件, 默认发送标准邮件
            javax.mail.Message message = null;
            if (mMailType == null || mMailType == MailConfig.emailType.NORMAL) {
                //创建普通文本消息
                message = mSendOneEmail.createTextEmail(session);
            }else if (mMailType == MailConfig.emailType.AttACHMENT){
                //创建混合邮件，包含正文和附件
                message = mSendOneEmail.createMixEmail(session);
            }
            //5 、 将message对象传递给transport对象，将邮件发送出去
            mTransport.sendMessage(message, message.getAllRecipients());

            //释放资源
        } catch (Exception e) {
            e.printStackTrace();
            exceptionList.add(e.getMessage());

        }finally {
            releaseMail();
        }

        return  exceptionList;
    }


    /**
     * 异步任务，用于收信或发信
     */
    private class MailTask extends AsyncTask<Object, Void, ArrayList<Object>>{

        private ProgressDialog dialog;
        boolean mFlag;
        public MailTask(){
            if (dialog == null){
                dialog = new ProgressDialog(mContext);
                dialog.setCanceledOnTouchOutside(false);
            }

            dialog.setMessage(mLoadingText);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.show();
        }

        @Override
        protected ArrayList<Object> doInBackground(Object... objects) {

            boolean flag = (boolean) objects[0];
            mFlag = flag;
            //当前邮件总数
            int currentMailCount = (int)objects[1];
            //每页请求的页数
            int pageSize = (int) objects[2];
            if (flag){
                //收邮件
               return receive(currentMailCount, pageSize);
            }else
            {
                //发邮件
               return send();
            }

        }

        @Override
        protected void onPostExecute(ArrayList<Object> dataOrException) {
            super.onPostExecute(dataOrException);
            dialog.dismiss();
            if (dataOrException != null && mFlag){
                //收邮件
                mReceiveEmailListener.receiveMailComplete(dataOrException);
            }else{
                //发邮件
                mSendEmailListener.onSendEmailComplete(dataOrException);
            }
        }
    }

    /**
     * 接收邮件
     * @param currentMailCount  当前邮件总数
     *  @param requestPages  一次请求的邮件数
     */
    private ArrayList<Object> receive(int currentMailCount, int requestPages){
        ArrayList<Object> list = new ArrayList<>();
        try {
            // 准备连接服务器的会话信息
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", MailConfig.POP3);       // 协议
            props.setProperty("mail.pop3.port", "110");             // 端口
            props.setProperty("mail.pop3.host", MailConfig.RECEIVE_SERVER);    // pop3服务器

            // 创建Session实例对象
            Session session = Session.getInstance(props, new EmailAuthenticator());
            mStore = session.getStore(MailConfig.POP3);
            mStore.connect();
            if (mStore.isConnected()){
                //成功连接socket,打开收件箱
                mFolder = mStore.getFolder("INBOX");
                mFolder.open(Folder.READ_ONLY);
               javax.mail.Message msgs[] ;
                //这是收件箱总数
                int count = mFolder.getMessageCount();
                if (requestPages <= 0 || count < requestPages){
                    //收取全部邮件,
                    msgs = mFolder.getMessages();
                }else{
                    //分页收取, 默认是从旧到新排序，所以从最后取，为最新邮件
                    int end = count - currentMailCount;
                    int start = end - requestPages - 1;
                    msgs = mFolder.getMessages(start, end);
                }

                if (msgs != null){
                    for (int i = msgs.length - 1; i >= 0; i--) {
                        list.add(parseEmailData(count, (MimeMessage) msgs[i]));
                    }
                }
                return  list;
            }


        }catch (Exception e){
            e.printStackTrace();
            list.add(e.getMessage());
        }finally {
            releaseMail();
        }

        return list;
    }

    /**
     * 接收邮件
     * @param currentMailCount 当前容器或显示的总邮件数，一般传容器的size
     * @param pageSize, 一次请求的邮件条目数
     */
    public void receiveMail(int currentMailCount, int pageSize){
        if (mMailTask == null){
            mMailTask = new MailTask();
        }
            mMailTask.execute(true, currentMailCount, pageSize);
    }
    /***
     * 释放socket资源, 内部已自动调用，也可以自己调用
     */
    public void releaseMail(){

        try{
            if (mFolder != null){
                mFolder.close(true);
            }

            if (mTransport != null){
                mTransport.close();
            }

            if (mStore != null){
                mStore.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void test(){
       javax.mail.util.SharedByteArrayInputStream inputStream;
    }

    /**
     * 从消息中解析邮件
     * @param totalMailCount
     * @param message
     * @return
     */
    private EmailModel parseEmailData(int totalMailCount, MimeMessage message){
        EmailModel model = new EmailModel();
        try{
            ReceiveOneEmail oneEmail = new ReceiveOneEmail(message);
            //拿到附件的文件名和附近流
            oneEmail.saveAttachmentsToList(message);
            //解析邮件内容
            oneEmail.getMailContent(message);
            //发件人
            model.setFrom(oneEmail.getFrom());
            //主题
            model.setSubject(oneEmail.getSubject());
            //邮件id
            model.setEmailId(oneEmail.getMessageId());
            model.setGetCC(oneEmail.getMailAddress("CC"));
            model.setToPerson(oneEmail.getMailAddress("TO"));
            //发送日期
            model.setSendTime(oneEmail.getSendDate());
            if (oneEmail.getHtmlBodyText().length() != 0){
                //如果是html类型的邮件就保存，
                model.setEmailContent(oneEmail.getHtmlBodyText());
            }else{
                //邮件内容为plain类型
                model.setEmailContent(oneEmail.getPlainBodyText());
            }
            model.setAttachmentModels(oneEmail.getAttachmentLists());
            //附件数量
            model.setAttachmentsCount(oneEmail.getAttachmentsCount());
            model.setTotalMailCount(totalMailCount);

            return  model;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    public static void main(String args []){
//
//        String toEmail = "chaimengge@ultrapower.com.cn";
//        //抄送
//        String ccEmail = "wangfeilong1@ultrapower.com.cn";
//        //密送
//       // String bccEmail = "zhangjinsong@ultrapower.com.cn";
//
//        File file = new File("D:\\" + "公司文档", "端口设置.jpg");
//        SendOneEmail sendOneEmail = new SendOneEmail();
//        sendOneEmail.addReceivePerson(toEmail);
//        sendOneEmail.addCC(ccEmail);
//       // sendOneEmail.addBCCEmailAdd(bccEmail);
//        sendOneEmail.setEmailContent("这是一封带附件的来自手机端的邮件");
//        sendOneEmail.setSendPerson("chaimengge@ultrapower.com.cn");
//        sendOneEmail.setSubject("带附件的邮件");
//        sendOneEmail.addAttachment(file);

        Mail mail = new Mail();
//        mail.setSendOneMail(sendOneEmail);
//        mail.mMailType = MailConfig.emailType.AttACHMENT;
       //mail.send();
            mail.receive(0, 1);

    }

    //测试发送带附近的邮件
    public SendOneEmail testSendAttachmentsMail(){
        String toEmail = "chaimengge@ultrapower.com.cn";
        //抄送
        String ccEmail = "wangfeilong1@ultrapower.com.cn";
        //密送
        String bccEmail = "zhangjinsong@ultrapower.com.cn";
        SendOneEmail sendOneEmail = new SendOneEmail();
        sendOneEmail.addReceivePerson(toEmail);
        sendOneEmail.addCC(ccEmail);
        sendOneEmail.addBCCEmailAdd(bccEmail);
        sendOneEmail.setEmailContent("我是来自一封手机端的邮件");
        sendOneEmail.setSendPerson("chaimengge@ultrapower.com.cn");
        sendOneEmail.setSubject("现在封你为宇宙大将军");

        return sendOneEmail;
    }


    //main方法测试普通文本邮件
    public SendOneEmail testSendNormalMail(){
        String toEmail = "chaimengge@ultrapower.com.cn";
        //抄送
        String ccEmail = "wangfeilong1@ultrapower.com.cn";
        //密送
        String bccEmail = "zhangjinsong@ultrapower.com.cn";
        SendOneEmail sendOneEmail = new SendOneEmail();
        sendOneEmail.addReceivePerson(toEmail);
        sendOneEmail.addCC(ccEmail);
        sendOneEmail.addBCCEmailAdd(bccEmail);
        sendOneEmail.setEmailContent("我是来自一封手机端的邮件");
        sendOneEmail.setSendPerson("chaimengge@ultrapower.com.cn");
        sendOneEmail.setSubject("现在封你为宇宙大将军");
        return  sendOneEmail;
    }

}
