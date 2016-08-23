package com.mengge.administrator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mengge.administrator.myemail.AttachUtil;
import com.mengge.administrator.myemail.AttachmentModel;
import com.mengge.administrator.myemail.EmailModel;
import com.mengge.administrator.myemail.Mail;
import com.mengge.administrator.myemail.R;
import com.mengge.administrator.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class EmailListActivity extends AppCompatActivity implements
        Mail.ReceiveEmailListener, OnItemClickListener, AdapterView.OnItemLongClickListener {
    ListView mMailList;

    List<EmailModel> emails;

    AttachUtil attachUtil;
    public static  void toActivity(Activity activity){
        Intent intent = new Intent(activity, EmailListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);
        mMailList = Util.f(this, R.id.emailList);
        mMailList.setOnItemClickListener(this);
        mMailList.setOnItemLongClickListener(this);
        receiveEmail();

        attachUtil = new AttachUtil();
    }

    Mail  mMail;
    void receiveEmail(){
        mMail = new Mail(this);
        mMail.setOnReceiveEmailListener(this);
        mMail.receiveMail(emails == null ? 0:emails.size(), 20);
    }

    @Override
    public void receiveMailComplete(ArrayList<Object> dataOrException) {
        if (emails == null)
        {
            emails = new ArrayList<>();
        }

        if (dataOrException.size() > 0 && dataOrException.get(0) instanceof  String){
            //有异常处理异常
            String exception = (String) dataOrException.get(0);
            Toast.makeText(this, "出错啦" + exception, Toast.LENGTH_SHORT).show();

        }else{

            for (int i = 0; i <dataOrException.size() ; i++) {
                EmailModel model = (EmailModel) dataOrException.get(i);
                emails.add(model);
            }
            mMailList.setAdapter(new MailListAdapter());
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String content = emails.get(i).getEmailContent();
        if (content != null){

            EmailDetailActivity.toActivity(this, content);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {

        ArrayList<AttachmentModel>  attachmentModels = (ArrayList<AttachmentModel>) emails.get(position).getAttachmentModels();
        if (attachmentModels != null && attachmentModels.size() > 0){

            for (int i = 0; i <attachmentModels.size() ; i++) {
                AttachmentModel mo = attachmentModels.get(i);
                attachUtil.saveAttachments(this, mo);
            }
        }else{
            Toast.makeText(this, "没有附件", Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    class MailListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return emails == null ? 0 : emails.size();
        }

        @Override
        public Object getItem(int i) {
            return emails == null ? null : emails.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
             MailHolder holder ;

            if (view == null){
                view = LayoutInflater.from(EmailListActivity.this).inflate(R.layout.item_mail_ist, viewGroup, false);
                holder = new MailHolder();

                holder.mailFrom = (TextView) view.findViewById(R.id.setEmailFrom);
                holder.mailSubject = (TextView) view.findViewById(R.id.setMailSubject);
                holder.mailSendTime = (TextView)view.findViewById(R.id.setSendTime);
                holder.mailAttachmentsCount = (TextView)view.findViewById(R.id.setAttachmentsCount);

                view.setTag(holder);

            }else
            {
                holder = (MailHolder) view.getTag();
            }

            EmailModel oneEmail = (EmailModel) getItem(i);
            holder.mailSendTime.setText(oneEmail.getSendTime());
            holder.mailFrom.setText(oneEmail.getFrom());
            holder.mailSubject.setText(oneEmail.getSubject());
            holder.mailAttachmentsCount.setText("附件个数" + oneEmail.getAttachmentsCount());
            return view;
        }
    }

    private class MailHolder {
        TextView mailFrom;
        TextView mailSubject;
        TextView mailSendTime;
        TextView mailAttachmentsCount;
    }
}
