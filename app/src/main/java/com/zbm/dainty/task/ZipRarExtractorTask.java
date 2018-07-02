package com.zbm.dainty.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.UnrarCallback;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class ZipRarExtractorTask extends AsyncTask<Void, Long, Long> {
    private final String TAG = "ZipRarExtractorTask";
    private File mInput;
    private File mOutput;
    private ProgressDialog mDialog;
    private long mProgress=0L;
    private long unPackSize;    //文件原始大小
    private boolean deleteZip;
    private String type;

    /**
     *
     * @param type 压缩文件类型
     * @param original 要解压的文件完整路径
     * @param purpose 解压到哪个目录
     * @param deleteZip 解压后是否删除压缩文件
     *
     * @return This instance of ZipRarExtractorTask.
     *
     */
    public ZipRarExtractorTask(Context context,String type, String original, String purpose, boolean deleteZip){
        super();
        this.type=type;
        this.deleteZip=deleteZip;

        mInput = new File(original);
        mOutput = new File(purpose);
        if(!mOutput.exists()){
            if(!mOutput.mkdirs()){
                Log.e(TAG, "Failed to make directories:"+mOutput.getAbsolutePath());
            }
        }
        if(context!=null){
            mDialog = new ProgressDialog(context);
        }
        else{
            mDialog = null;
        }

    }
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        Log.d(TAG,"调用onPreExecute");
        if(mDialog!=null){
            mDialog.setTitle("解压中...");
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDialog.cancel();
                }
            });
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage(mInput.getName());
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    cancel(true);
                }
            });
            mDialog.show();
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        // TODO Auto-generated method stub
        switch (type){
            case "zip":
                return unzip();
            case "rar":
                return unRar();
            default:
                return 0L;
        }

    }

    @Override
    protected void onProgressUpdate(Long... values) {
        // TODO Auto-generated method stub
        if(mDialog==null)
            return;
        if(values.length>1){
            unPackSize=values[1];
            mDialog.setMax(100);
        }
        else
            mDialog.setProgress((int) ((double)values[0]/unPackSize*100.0));
    }

    @Override
    protected void onPostExecute(Long result) {
        // TODO Auto-generated method stub
        if (deleteZip){
            mInput.delete();
        }
        if(mDialog!=null&&mDialog.isShowing()){
            mDialog.dismiss();
        }
        mInput=null;
        mOutput=null;

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mOutput.delete();
        Log.d("Extractor","取消解压");
    }

    private long unzip(){
        long extractedSize = 0L;
        Enumeration<ZipEntry> entries;  //含有被压缩的文件对象（目录或文件）
        ZipFile zip = null;
        try {
            zip = new ZipFile(mInput,"GBK");
            long uncompressedSize = getOriginalSize(zip);
            publishProgress(0L, uncompressedSize);
            entries = zip.getEntries();
            while(entries.hasMoreElements()){
                if(isCancelled())
                    break;
                ZipEntry entry = entries.nextElement();
                Log.d(TAG,"entry :"+entry.getName());
                if(entry.isDirectory()){
                    continue;   //目录内的文件对象会在下次遍历中会出现
                }
                File destination = new File(mOutput, entry.getName());  //每个文件的存放路径，包括文件夹内的

                if(!destination.getParentFile().exists()){
                    Log.e(TAG, "make="+destination.getParentFile().getAbsolutePath());
                    destination.getParentFile().mkdirs();
                }

                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
                extractedSize+=save(zip.getInputStream(entry),outStream);
                outStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return extractedSize;
    }

    private long unRar(){
        long extractedSize = 0L;
        Archive rarFile=null;
        try {
            rarFile=new Archive(mInput, new UnrarCallback() {
                @Override
                public boolean isNextVolumeReady(File file) {
                    return false;
                }

                @Override
                public void volumeProgressChanged(long l, long l1) {
                    publishProgress(l);
                }
            });
            long uncompressedSize = getOriginalSize(rarFile);
            publishProgress(0L, uncompressedSize);

            for (int i=0;i<rarFile.getFileHeaders().size();i++){
                if(isCancelled())
                    break;
                FileHeader fh=rarFile.getFileHeaders().get(i);
                String entryPath;
                if (fh.isUnicode()){
                    entryPath = fh.getFileNameW().trim();
                }else{
                    entryPath = fh.getFileNameString().trim();
                }
                entryPath = entryPath.replaceAll("\\\\", "/");
                File file=new File(mOutput,entryPath);
                if (fh.isDirectory()){
                    file.mkdirs();
                }else {
                    File parent=file.getParentFile();
                    if (parent!=null&&!parent.exists()){
                        parent.mkdirs();
                    }
                 FileOutputStream fileOut=new FileOutputStream(file);
                    rarFile.extractFile(fh,fileOut);
                    fileOut.close();
                }
            }

        } catch (RarException | IOException e) {
            e.printStackTrace();
        }finally {
            if (rarFile!=null) {
                try {
                    rarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return extractedSize;
    }
    /**
     *
     * @param file 压缩文件对象
     * @return 放回压缩文件的原始大小
     */
    private long getOriginalSize(ZipFile file){
        Enumeration<ZipEntry> entries = file.getEntries();
        long originalSize = 0L;
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.getSize()>=0){
                originalSize+=entry.getSize();
            }
        }
        return originalSize;
    }

    private long getOriginalSize(Archive file){
        long originalSize = 0L;
        for (int i=0;i<file.getFileHeaders().size();i++){
            FileHeader fh=file.getFileHeaders().get(i);
            if (fh.getUnpSize()>=0){
                originalSize+=fh.getPackSize();
            }
        }
        return originalSize;
    }
    /**
     * 该方法主要是将压缩文件内的所有对象提取保存到指定路径
     * @param input  输入路
     * @param output 封装好的可以记录解压进度的输出流
     * @return 统计缓存大小
     */
    private int save(InputStream input, OutputStream output){
        byte[] buffer = new byte[1024*8];
        BufferedInputStream in = new BufferedInputStream(input, 1024*8);
        BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
        int count =0,n;
        try {
            while((n=in.read(buffer, 0, 1024*8))!=-1){
                out.write(buffer, 0, n);
                count+=n;
            }
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return count;
    }

    private final class ProgressReportingOutputStream extends FileOutputStream {

        ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void write(@NonNull byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            // TODO Auto-generated method stub
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            publishProgress(mProgress);
        }

    }
}
