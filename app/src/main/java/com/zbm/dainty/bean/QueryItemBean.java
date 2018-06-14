package com.zbm.dainty.bean;

/**
 * Created by Zbm阿铭 on 2018/1/25.
 */

public class QueryItemBean {
    private int queryID;
    private String queryTYPE,queryTIME,queryNAME;

    public QueryItemBean(int queryID, String queryTYPE, String queryTIME, String queryNAME){
        this.queryID=queryID;
        this.queryTYPE=queryTYPE;
        this.queryTIME=queryTIME;
        this.queryNAME=queryNAME;
    }

    public int getQueryID() {
        return queryID;
    }

    public void setQueryID(int queryID) {
        this.queryID = queryID;
    }

    public String getQueryTYPE() {
        return queryTYPE;
    }

    public void setQueryTYPE(String queryTYPE) {
        this.queryTYPE = queryTYPE;
    }

    public String getQueryTIME() {
        return queryTIME;
    }

    public void setQueryTIME(String queryTIME) {
        this.queryTIME = queryTIME;
    }

    public String getQueryNAME() {
        return queryNAME;
    }

    public void setQueryNAME(String queryNAME) {
        this.queryNAME = queryNAME;
    }
}
