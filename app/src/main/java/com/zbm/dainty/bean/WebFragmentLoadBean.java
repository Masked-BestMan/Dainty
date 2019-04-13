package com.zbm.dainty.bean;

public class WebFragmentLoadBean {
    private int loadInMode;
    private String url;

    public WebFragmentLoadBean(int loadInMode, String url) {
        this.loadInMode = loadInMode;
        this.url = url;
    }

    public int getLoadInMode() {
        return loadInMode;
    }

    public void setLoadInMode(int loadInMode) {
        this.loadInMode = loadInMode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
