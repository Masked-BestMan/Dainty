package com.zbm.dainty.util;

/**
 * Created by Zbm阿铭 on 2017/11/18.
 */

public interface IDockingController {
    int DOCKING_HEADER_HIDDEN = 1;
    int DOCKING_HEADER_DOCKING = 2;
    int DOCKING_HEADER_DOCKED = 3;

    int getDockingState(int firstVisibleGroup, int firstVisibleChild);
}
