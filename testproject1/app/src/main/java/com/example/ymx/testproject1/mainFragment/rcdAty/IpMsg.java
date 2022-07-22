package com.example.ymx.testproject1.mainFragment.rcdAty;

import android.widget.ImageView;
import org.litepal.crud.DataSupport;

public class IpMsg extends DataSupport{
    public static final int TYPE_RECEIVE = 0;
    public static final int TYPE_SENT = 1;
    private String time;
    private String ipAddress;
    private String content;
    private int type;

    public IpMsg(String content,int type){
        this.content = content;
        this.type = type;
    }

    public IpMsg(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public String getContent(){
        return content;
    }

    public String getIpAddress(){return ipAddress;}

    public int getType(){
        return type;
    }

    public String getTime() {
        return time;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTime(String time) {
        this.time = time;
    }
}