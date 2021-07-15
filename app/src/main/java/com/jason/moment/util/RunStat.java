package com.jason.moment.util;

import java.util.ArrayList;

public class RunStat {
    public long lastPk=-1;
    public ArrayList<MyActivity> list=null;
    public long running_id=-1;
    public RunStat(ArrayList<MyActivity> list) {
        this.list = list;
    }
    public RunStat(ArrayList<MyActivity> list, long lastPk, long running_id) {
        this.list = list;
        this.lastPk = lastPk;
        this.running_id=running_id;
    }


}
