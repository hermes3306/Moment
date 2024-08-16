package com.jason.moment.util;

import com.jason.moment.activity.Run;

import java.util.ArrayList;

public class RunStat {
    public long lastPk=-1;
    public ArrayList<MyActivity> list=null;
    public long running_id=-1;
    public Run run;
    public RunStat(Run run, ArrayList<MyActivity> list) {
        this.run = run;
        this.list = list;
    }
    public RunStat(Run run, ArrayList<MyActivity> list, long lastPk, long running_id) {
        this.run = run;
        this.list = list;
        this.lastPk = lastPk;
        this.running_id=running_id;
    }


}
