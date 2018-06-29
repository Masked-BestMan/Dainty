package com.zbm.dainty.bean;

import java.io.Serializable;

/**
 * Created by Zbm阿铭 on 2017/11/16.
 */

    public class HistoryItemBean implements Serializable{
    private int historyID;
    private String historyNAME,historyURI,historyTIME;
    public HistoryItemBean(int historyID,String historyNAME,String historyURI){
        this.historyID=historyID;
        this.historyNAME=historyNAME;
        this.historyURI=historyURI;
    }

    public int getHistoryID() {
        return historyID;
    }


    public String getHistoryNAME() {
        return historyNAME;
    }

    public String getHistoryURI() {
        return historyURI;
    }

    @SuppressWarnings("unused")
    public String getHistoryTIME() {
        return historyTIME;
    }

    @SuppressWarnings("unused")
    public void setHistoryTIME(String historyTIME) {
        this.historyTIME = historyTIME;
    }

}
