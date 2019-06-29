package com.remmoo997.igtvsaver.utils;

import java.util.ArrayList;

class Schema {

    private final ArrayList<String> multi_data = new ArrayList<>();
    private String SINGLE_POST_PICTURE_URL;
    private String SINGLE_POST_VIDEO_URL;

    // User info
    private String USERNAME;
    private int FOLLOWERS;
    private int FOLLOWING;
    private String USERNAME_PICTURE_URL;

    Schema() {}

    Schema(String type, String data) {
        switch (type) {
            case "SinglePicture":
                this.SINGLE_POST_PICTURE_URL = data;
                break;
            case "SingleVideo":
                this.SINGLE_POST_VIDEO_URL = data;
                break;
        }
    }

    Schema(String profile, String username, int followers, int following) {
        this.USERNAME_PICTURE_URL = profile;
        this.USERNAME = username;
        this.FOLLOWERS = followers;
        this.FOLLOWING = following;
    }

    public ArrayList<String> getMulti_data() {
        return multi_data;
    }

    String getUSERNAME() {
        return USERNAME;
    }

    int getFOLLOWERS() {
        return FOLLOWERS;
    }

    int getFOLLOWING() {
        return FOLLOWING;
    }

    String getSINGLE_POST_PICTURE_URL() {
        return SINGLE_POST_PICTURE_URL;
    }

    String getSINGLE_POST_VIDEO_URL() {
        return SINGLE_POST_VIDEO_URL;
    }

    String getUSERNAME_PICTURE_URL() {
        return USERNAME_PICTURE_URL;
    }

}
