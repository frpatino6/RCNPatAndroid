package com.rcn.pat.Global;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rcn.pat.R;

public class CustomListViewDialog extends Dialog implements View.OnClickListener {


    public Activity activity;
    public Dialog dialog;
    public Button yes, no;
    TextView title;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public CustomListViewDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public CustomListViewDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public CustomListViewDialog(Activity a, RecyclerView.Adapter adapter) {
        super(a);
        this.activity = a;
        this.adapter = adapter;
        setupLayout();
    }

    private void setupLayout() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yes:
                //Do Something
                break;
            case R.id.no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog_layout);
        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);
        title = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);


        recyclerView.setAdapter(adapter);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }
}
