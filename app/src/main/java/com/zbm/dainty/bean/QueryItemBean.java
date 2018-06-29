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

    @SuppressWarnings("unused")
    public int getQueryID() {
        return queryID;
    }


    public String getQueryTYPE() {
        return queryTYPE;
    }


    @SuppressWarnings("unused")
    public String getQueryTIME() {
        return queryTIME;
    }


    public String getQueryNAME() {
        return queryNAME;
    }

}
