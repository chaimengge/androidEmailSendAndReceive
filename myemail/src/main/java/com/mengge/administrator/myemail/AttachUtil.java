package com.mengge.administrator.myemail;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.mengge.administrator.utils.EmailUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/8/23.
 * 保存附件
 */
public class AttachUtil {

    private HashMap<String ,Boolean> saveMaps = new HashMap<>();
    private static final String ATTACHMENTS_DIR_NAME = "attachments";

    private static final int SAVING = 'A' << 12;
    private static final int NO_SDCARD = SAVING << 1;
    private static final int EXISTED = NO_SDCARD << 2;
    private static final int DONE = EXISTED << 3;
    private Context mContext;
    public  void saveAttachments(Context c, AttachmentModel model){
        mContext = c;
        new SaveAttachTask().execute(model);
    }

    private class SaveAttachTask extends AsyncTask<Object, Integer, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            AttachmentModel model = (AttachmentModel) params[0];
            String fileName = model.getFileName();
            InputStream inputStream = model.getInputStream();
            int total = model.getAttachmentsLength();
                if (!EmailUtil.isHaveSDCard())
                {
                   return NO_SDCARD;
                }
                //sdcard路径
                String sdcardPath = Environment.getExternalStorageDirectory().getPath();
                //创建附件文件夹路径
                String attachmentPath = sdcardPath + File.separator + ATTACHMENTS_DIR_NAME;
                File dir = new File(attachmentPath);
                if (!dir.exists())
                {
                    dir.mkdirs();
                }
                File file = new File(attachmentPath, fileName);

                if (file.exists()){
                    return EXISTED;
                }
                if (saveMaps.get(fileName) != null && saveMaps.get(fileName)){
                    return SAVING;
                }

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                try {
                    saveMaps.put(fileName, true);
                    bis =  new BufferedInputStream(inputStream);

                    bos = new BufferedOutputStream(new FileOutputStream(file));


                    int len ;
                     //设置成有效字节，要不出问题
                    byte [] buffer = new byte [inputStream.available()];
                    while ((len = inputStream.available()) > 0){

                        publishProgress(len, total);

                        int result = inputStream.read(buffer);
                        if (result == -1){
                            break;
                        }
                    }
                    bos.write(buffer);
                    saveMaps.put(fileName, false);
                    return  DONE;

                }catch (Exception e){
                    e.printStackTrace();
                    saveMaps.put(fileName, false);
                }finally {
                    try{

                        if (bis != null){
                            bis.close();
                        }

                        if (bos != null){
                            bos.close();
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            switch (integer){
                case NO_SDCARD:
                    Toast.makeText(mContext, "没有检测到SDCard", Toast.LENGTH_SHORT).show();
                    break;

                case SAVING:
                    Toast.makeText(mContext, "正在保存附件", Toast.LENGTH_SHORT).show();
                    break;
                case EXISTED:
                    Toast.makeText(mContext,  "文件已存在", Toast.LENGTH_SHORT).show();
                    break;

                case DONE:
                    Toast.makeText(mContext,  "文件已保存", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }



}
