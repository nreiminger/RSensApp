package com.example.aird.rsens_user.controller;

public class NotificationG {
    static boolean notif = false;
    static boolean pm40 = false;

    static public boolean getNotif() {
        return notif;
    }

    static public void setNotif(boolean notif) {
        NotificationG.notif = notif;
    }

    static public boolean getPm40() {
        return pm40;
    }

    static public void setPm40(boolean pm40) {
        NotificationG.pm40 = pm40;
    }
}
