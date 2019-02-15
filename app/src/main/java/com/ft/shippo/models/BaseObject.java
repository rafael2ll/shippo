package com.ft.shippo.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rafael on 21/01/18.
 */

public class BaseObject {
    @SerializedName("_id")
    String id;
    private String updatedAt;
    String created_at;

    public String getId() {
        return id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
