package com.androidscanapp.data;


public class LinkData {
    public long id;
    public String url;
    public String postData;
    public String suspect;

    public String toString(){
        return "ID:"+id+" URL: "+url+" suspect: "+suspect+"postData: "+postData;
    }
}
