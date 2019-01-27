package com.zbm.dainty.decompression;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.file.zip.ZipEntry;
import com.file.zip.ZipFile;
import com.zbm.dainty.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.UnrarCallback;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DecompressionModel implements DecompressionContract.Model {
    private long mProgress;

    private File dir;  //临时文件目录

    DecompressionModel(Context context) {
        dir = new File(context.getExternalCacheDir() + "/compression_temp/");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    @Override
    public Observable<Long> decompression(final String type, String original, String purpose, boolean deleteZip) {
        final File mInput = new File(original);
        final File mOutput = new File(purpose);
        mProgress = 0L;
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                switch (type) {
                    case "zip":
                        unzip(mInput, mOutput, emitter);
                        break;
                    case "rar":
                        unRar(mInput, mOutput, emitter);
                        break;
                    default:
                        emitter.onError(new Throwable("无法解压该类型的压缩文件"));
                        break;
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public Observable<Intent> resolveFileContent(final String type, final String srcPath, final Object object) {
        return Observable.create(new ObservableOnSubscribe<Intent>() {
            @Override
            public void subscribe(ObservableEmitter<Intent> emitter) throws Exception {
                FileOutputStream fileOutputStream = null;
                File outputPath = null;
                switch (type) {
                    case "zip":
                        ZipEntry entry = (ZipEntry) object;
                        String zipFileName = FileUtil.getFileNameFromPath(entry.getName());
                        outputPath = new File(dir, zipFileName);
                        ZipFile zipFile = null;

                        try {
                            zipFile = new ZipFile(srcPath, "GBK");
                            fileOutputStream = new FileOutputStream(outputPath);
                            save(zipFile.getInputStream(entry), fileOutputStream);
                        } finally {
                            if (fileOutputStream != null) {
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
                        FileHeader fh = (FileHeader) object;
                        String entryPath;
                        if (fh.isUnicode()) {
                            entryPath = fh.getFileNameW().trim();
                        } else {
                            entryPath = fh.getFileNameString().trim();
                        }
                        entryPath = entryPath.replaceAll("\\\\", "/");
                        String rarFileName = FileUtil.getFileNameFromPath(entryPath);
                        outputPath = new File(dir, rarFileName);
                        Archive rarFile = null;
                        try {
                            rarFile = new Archive(new File(srcPath));
                            fileOutputStream = new FileOutputStream(outputPath);
                            rarFile.extractFile(fh, fileOutputStream);
                        } finally {
                            if (fileOutputStream != null) {
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
                if (outputPath == null) {
                    emitter.onError(new Throwable("读取文件失败"));
                } else {
                    emitter.onNext(FileUtil.getFileIntent(outputPath));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Object>> resolveZip(final String original) {
        return Observable.create(new ObservableOnSubscribe<List<Object>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Object>> emitter) throws Exception {
                List<Object> data = new ArrayList<>();
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(original, "GBK");
                    Enumeration<ZipEntry> entries = zipFile.getEntries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (!entry.isDirectory()) {
                            data.add(entry);
                        }
                    }
                    emitter.onNext(data);
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Object>> resolveRar(final String original) {
        return Observable.create(new ObservableOnSubscribe<List<Object>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Object>> emitter) throws Exception {
                List<Object> data = new ArrayList<>();
                Archive rarFile = null;
                try {
                    rarFile = new Archive(new File(original));
                    FileHeader fh = rarFile.nextFileHeader();
                    while (fh != null) {

                        if (!fh.isDirectory()) {
                            data.add(fh);
                        }
                        fh = rarFile.nextFileHeader();
                    }
                    emitter.onNext(data);
                } finally {
                    if (rarFile != null) {
                        try {
                            rarFile.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void unzip(File mInput, File mOutput, ObservableEmitter<Long> emitter) throws IOException {
        Enumeration<ZipEntry> entries;  //含有被压缩的文件对象（目录或文件）
        ZipFile zip = null;
        try {
            zip = new ZipFile(mInput, "GBK");
            long uncompressedSize = getOriginalSize(zip);
            //publishProgress(0L, uncompressedSize);
            emitter.onNext(uncompressedSize);
            entries = zip.getEntries();
            while (entries.hasMoreElements()) {
                if (emitter.isDisposed())
                    break;
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;   //目录内的文件对象会在下次遍历中会出现
                }
                File destination = new File(mOutput, entry.getName());  //每个文件的存放路径，包括文件夹内的

                if (!destination.getParentFile().exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    destination.getParentFile().mkdirs();
                }

                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination, emitter);
                save(zip.getInputStream(entry), outStream);
                outStream.close();
            }
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void unRar(File mInput, File mOutput, final ObservableEmitter<Long> emitter) throws IOException, RarException {
        Archive rarFile = null;
        try {
            rarFile = new Archive(mInput, new UnrarCallback() {
                @Override
                public boolean isNextVolumeReady(File file) {
                    return false;
                }

                @Override
                public void volumeProgressChanged(long l, long l1) {
                    emitter.onNext(l);
                }
            });
            long uncompressedSize = getOriginalSize(rarFile);
            //publishProgress(0L, uncompressedSize);
            emitter.onNext(uncompressedSize);

            for (int i = 0; i < rarFile.getFileHeaders().size(); i++) {
                if (emitter.isDisposed())
                    break;
                FileHeader fh = rarFile.getFileHeaders().get(i);
                String entryPath;
                if (fh.isUnicode()) {
                    entryPath = fh.getFileNameW().trim();
                } else {
                    entryPath = fh.getFileNameString().trim();
                }
                entryPath = entryPath.replaceAll("\\\\", "/");
                File file = new File(mOutput, entryPath);
                if (fh.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        parent.mkdirs();
                    }
                    FileOutputStream fileOut = new FileOutputStream(file);
                    rarFile.extractFile(fh, fileOut);
                    fileOut.close();
                }
            }
            emitter.onComplete();
        } finally {
            if (rarFile != null) {
                try {
                    rarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param file 压缩文件对象
     * @return 放回压缩文件的原始大小
     */
    private long getOriginalSize(ZipFile file) {
        Enumeration<ZipEntry> entries = file.getEntries();
        long originalSize = 0L;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getSize() >= 0) {
                originalSize += entry.getSize();
            }
        }
        return originalSize;
    }

    private long getOriginalSize(Archive file) {
        long originalSize = 0L;
        for (int i = 0; i < file.getFileHeaders().size(); i++) {
            FileHeader fh = file.getFileHeaders().get(i);
            if (fh.getUnpSize() >= 0) {
                originalSize += fh.getPackSize();
            }
        }
        return originalSize;
    }

    /**
     * 该方法主要是将压缩文件内的所有对象提取保存到指定路径
     *
     * @param input  输入路
     * @param output 封装好的可以记录解压进度的输出流
     * @return 统计缓存大小
     */
    @SuppressWarnings("UnusedReturnValue")
    private int save(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
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
        return count;
    }

    private final class ProgressReportingOutputStream extends FileOutputStream {
        private ObservableEmitter<Long> emitter;

        ProgressReportingOutputStream(File file, ObservableEmitter<Long> emitter)
                throws FileNotFoundException {
            super(file);
            this.emitter = emitter;
        }

        @Override
        public void write(@NonNull byte[] buffer, int byteOffset, int byteCount)
                throws IOException {
            // TODO Auto-generated method stub
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            //publishProgress(mProgress);
            emitter.onNext(mProgress);
        }

    }
}
