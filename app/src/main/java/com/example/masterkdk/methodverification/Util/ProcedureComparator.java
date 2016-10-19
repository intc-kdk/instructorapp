package com.example.masterkdk.methodverification.Util;

import com.example.masterkdk.methodverification.Util.DataStructureUtil.ProcItem;

import java.util.Comparator;

/**
 * Created by takashi on 2016/10/15.
 */

public class ProcedureComparator implements Comparator<ProcItem> {
    @Override
    public int compare(ProcItem o1, ProcItem o2) {
        return o1.in_sno < o2.in_sno ? -1 : 1 ;
    }
}
