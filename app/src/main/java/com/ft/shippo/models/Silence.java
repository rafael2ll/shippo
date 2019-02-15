package com.ft.shippo.models;

import com.orm.SugarRecord;

/**
 * Created by rafael on 09/03/18.
 */

public class Silence extends SugarRecord {
    String user_id;
    String start_time;
    String end_time;
    int option;
    public  Silence(){}

    public Silence(String user_id, String start_time, String end_time, int option) {
        this.user_id = user_id;
        this.option = option;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    public int getOption() {
        return option;
    }

    public String getEnd() {
        return end_time;
    }
}
