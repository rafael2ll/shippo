package com.ft.shippo.models;

import android.support.annotation.RawRes;

/**
 * Created by rafael on 31/01/18.
 */

public class Country extends BaseObject {
    private String name;
    private String code;
    private @RawRes
    int icon = 0;

    public Country() {

    }

    public Country(String name, String code, int icon) {
        this.name = name;
        this.code = code;
        this.icon = icon;
    }

    public Country(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(@RawRes int icon) {
        this.icon = icon;
    }
}
