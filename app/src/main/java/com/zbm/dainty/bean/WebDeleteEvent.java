package com.zbm.dainty.bean;

/**
 * Created by Zbm阿铭 on 2017/11/8.
 */

public class WebDeleteEvent {
    private int viewTop;
    public WebDeleteEvent(int top){
        viewTop=top;
    }
    public int getViewTop(){
        return viewTop;
    }
}
