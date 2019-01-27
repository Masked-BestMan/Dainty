package com.zbm.dainty.decompression;

import android.content.Intent;

import com.zbm.dainty.base.BaseModel;
import com.zbm.dainty.base.BasePresenter;
import com.zbm.dainty.base.BaseView;

import java.util.List;

import io.reactivex.Observable;

public interface DecompressionContract {

    interface View extends BaseView {
        void showResolvedCompressedFile(List<Object> data);

        void showResolveFileContent(Intent intent);
    }

    interface Presenter extends BasePresenter {
        void decompression(String type, String original, String purpose, boolean deleteZip);

        void resolveFileContent(String type, String srcPath, Object object);

        void resolveZip(String original);

        void resolveRar(String original);
    }

    interface Model extends BaseModel {
        Observable<Long> decompression(String type, String original, String purpose, boolean deleteZip);

        Observable<Intent> resolveFileContent(String type, String srcPath, Object object);

        Observable<List<Object>> resolveZip(String original);

        Observable<List<Object>> resolveRar(String original);
    }
}
