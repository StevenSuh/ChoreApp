package com.example.choreapp;

import android.view.MotionEvent;
import android.view.View;

public class Utils {

    public static void setTouchEffect(View view, final boolean goDark) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(goDark ? 0.5f : 1f);
                } else {
                    v.setAlpha(goDark ? 1f : 0.5f);
                }
                return false;
            }
        });
    }
}
