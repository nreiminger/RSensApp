package com.example.aird.rsens_user.models;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * class specifique au bouton utilise pour les activity DayActivity et PullActivity
 */

public class UrlButton extends Button {

    private String url;

    public UrlButton(Context context) {
        super(context);
    }
    public UrlButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public UrlButton(Context context, String url_days) {
        super(context);
        this.url = url_days;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
