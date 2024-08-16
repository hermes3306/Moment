package com.jason.moment.util;

import java.util.Date;

public class MyRunInfo {
    public long run_id;
    public Date cr_date;
    public int status;

    public MyRunInfo(long run_id, Date cr_date, int status) {
        this.run_id = run_id;
        this.cr_date = cr_date;
        this.status = status;
    }

    public String toString() {
        return "("+ run_id + "," + cr_date + "," + status + ")";
    }

}
