package com.dom.ination.domforandroid.support.paging;

import java.io.Serializable;

/**
 * Created by 10174987 on 2016/8/29.
 */

public interface IPaging<T extends Serializable, Ts extends Serializable> extends Serializable {
    void processData(Ts newDatas, T firstData, T lastData);
    String getPreviousPage();
    String getNextPage();
}
