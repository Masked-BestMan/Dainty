package com.zbm.dainty.decompression;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class DecompressionPresenter implements DecompressionContract.Presenter {
    private DecompressionModel model;
    private CompositeDisposable disposable;
    private DecompressionContract.View view;
    private ProgressDialog mDialog;

    DecompressionPresenter(DecompressionContract.View view) {
        model = new DecompressionModel((Activity) view);
        disposable = new CompositeDisposable();
        this.view=view;
        mDialog = new ProgressDialog((Activity) view);
    }

    @Override
    public void decompression(String type, String original, String purpose, boolean deleteZip) {
        final long[] unPackSize = {0};
        mDialog.setTitle("解压中...");
        mDialog.setMax(100);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDialog.cancel();
                disposable.clear();
            }
        });
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setMessage(new File(original).getName());
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.show();

        disposable.add(model.decompression(type, original, purpose, deleteZip)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
                        Log.d("ttt"," "+aLong);
                        if (unPackSize[0]==aLong){
                            mDialog.cancel();
                            view.showToast("解压成功");
                        }
                        if (unPackSize[0] == 0L) {
                            unPackSize[0] = aLong;
                        } else {
                            mDialog.setProgress((int) ((double) aLong / unPackSize[0] * 100.0));
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        view.showToast("解压失败");
                    }
                }));
    }

    @Override
    public void resolveFileContent(String type, String srcPath,Object object) {
        disposable.add(model.resolveFileContent(type,srcPath,object).subscribe(new Consumer<Intent>() {
            @Override
            public void accept(Intent intent) {
                view.showResolveFileContent(intent);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                view.showToast("解析文件失败");
            }
        }));
    }

    @Override
    public void resolveZip(String original) {
        disposable.add(model.resolveZip(original).subscribe(new Consumer<List<Object>>() {
            @Override
            public void accept(List<Object> list) {
                view.showResolvedCompressedFile(list);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                view.showToast("解析失败");
            }
        }));
    }

    @Override
    public void resolveRar(String original) {
        disposable.add(model.resolveRar(original).subscribe(new Consumer<List<Object>>() {
            @Override
            public void accept(List<Object> list) {
                view.showResolvedCompressedFile(list);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                view.showToast("解析失败");
            }
        }));
    }

    @Override
    public void unsubscribe() {
        disposable.clear();
    }
}
