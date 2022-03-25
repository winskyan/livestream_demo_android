package io.agora.livedemo.data.restapi.model;

import io.agora.livedemo.data.model.BaseBean;
import com.google.gson.annotations.SerializedName;

/**
 * Created by wei on 2017/3/8.
 */

public class ResponseModule<T> extends BaseBean {
    @SerializedName("entities")
    public T data;
    public int count;
    public String cursor;
}
