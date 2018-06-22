package com.zbm.dainty.bean;

public class HistoryPositionBean {
    private int child,parent;
    public HistoryPositionBean(int child,int parent){
        this.child=child;
        this.parent=parent;
    }

    public int getChild() {
        return child;
    }

    public int getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return parent+" "+child;
    }

    @Override
    public boolean equals(Object obj) {
        HistoryPositionBean positionBean;
        if (obj instanceof HistoryPositionBean) {
            positionBean= (HistoryPositionBean) obj;
            return child==positionBean.child&&parent==positionBean.parent;
        } else
            return super.equals(obj);
    }
}
