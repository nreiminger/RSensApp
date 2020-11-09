package com.example.aird.rsens_user.models;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Class qui permet l'extension de Button pour qu'on stock l'id du capteur dedans
 */
public class CapteurButton extends Button {

    private static String id=null;

    public CapteurButton(Context context) {
        super(context);
    }
    public CapteurButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CapteurButton(Context context, String id) {
        super(context);
        this.id = id;
    }
    public static String getid() {
        return id;
    }

    public static void setid(String id) {
        CapteurButton.id = id;
    }
}
