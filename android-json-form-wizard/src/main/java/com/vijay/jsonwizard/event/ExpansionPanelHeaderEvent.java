package com.vijay.jsonwizard.event;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ExpansionPanelHeaderEvent extends BaseEvent {
    private String title;
    private TextView editText;

    public ExpansionPanelHeaderEvent(String title, TextView editText) {
        this.title = title;
        this.editText = editText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TextView getEditText() {
        return editText;
    }

    public void setEditText(TextView editText) {
        this.editText = editText;
    }
}
