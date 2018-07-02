package com.zbm.dainty.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;
import com.zbm.dainty.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;


/**
 * 临时将压缩文件内的某个文件提取出来，用以打开
 */
public class ResolveFileContentTask extends AsyncTask<Object, Void, File> {
    private WeakReference<Context> contextReference;
    private String srcPath;
    private File dir;  //临时文件目录
    private String type;

    public ResolveFileContentTask(Context context, String srcPath,String type) {
        contextReference = new WeakReference<>(context);
        this.srcPath = srcPath;
        this.type=type;

        Log.d("rer","压缩文件："+srcPath);
        dir=new File(context.getExternalCacheDir() + "/compression_temp/");
        if (!dir.exists())
            dir.mkdirs();


    }

    @Override
    protected File doInBackground(Object... objects) {
        FileOutputStream fileOutputStream=null;
        File outputPath=null;
        switch (type) {
            case "zip":
                ZipEntry entry= (ZipEntry) objects[0];
                String zipFileName = FileUtil.getFileNameFromPath(entry.getName());
                outputPath = new File(dir, zipFileName);
                ZipFile zipFile=null;

                try {
                    Log.d("rer",entry.getName());
                    zipFile=new ZipFile(srcPath,"GBK");
                    fileOutputStream=new FileOutputStream(outputPath);
                    save(zipFile.getInputStream(entry), fileOutputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fileOutputStream!=null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case "rar":
                FileHeader fh= (FileHeader) objects[0];
                String entryPath;
                if (fh.isUnicode()){
                    entryPath = fh.getFileNameW().trim();
                }else{
                    entryPath = fh.getFileNameString().trim();
                }
                entryPath = entryPath.replaceAll("\\\\", "/");
                String rarFileName = FileUtil.getFileNameFromPath(entryPath);
                outputPath = new File(dir, rarFileName);
                Archive rarFile=null;
                try {
                    rarFile=new Archive(new File(srcPath));
                    fileOutputStream=new FileOutputStream(outputPath);
                    rarFile.extractFile(fh,fileOutputStream);
                } catch (RarException | IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fileOutputStream!=null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (rarFile != null) {
                        try {
                            rarFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
        return outputPath;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        Context context = contextReference.get();
        if (context != null) {
            if (file == null)
                Toast.makeText(context, "读取文件失败", Toast.LENGTH_SHORT).show();
            else {
                Log.d("ResolveFile",file.getPath());
                context.startActivity(FileUtil.getFileIntent(file));
            }
        }

    }

    private void save(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int n;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
