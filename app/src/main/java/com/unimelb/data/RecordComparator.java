package com.unimelb.data;

import java.util.Comparator;

/**
 * Created by xialeizhou on 10/10/15.
 */
public class RecordComparator implements Comparator<Record> {
    @Override
    public int compare(Record r1, Record r2) {
        if (r1.getDate().compareTo(r2.getDate()) > 0) {
            return 1;
        } else if (r1.getDate().compareTo(r2.getDate()) < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
