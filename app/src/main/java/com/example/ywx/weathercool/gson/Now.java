package com.example.ywx.weathercool.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ywx on 2017/4/5.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More
    {
        @SerializedName("info")
        public String info;
    }
}
