package com.merdeev.experiment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, CompleteListener {

    final static String LOG = "States";
    private TextView tvContent;
    private ArrayList<Map<String, String>> list;
    private String offset = "";
    private String address = "";
    private final int DIALOG_LIST = 1;
    private String list_title;
    private String resource;
    private String reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG, "mainActivity: onCreate");

        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());
        Button btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(this);

        list_title = getResources().getString(R.string.list_title);
        resource = getResources().getString(R.string.resource);
        reference = getResources().getString(R.string.reference);

        doRequestList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnList:
                Log.d(LOG, "btnList: onClick");
                offset = "";
                doRequestList();
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
        switch (i) {
            case Dialog.BUTTON_NEUTRAL:
                break;

            case Dialog.BUTTON_NEGATIVE:
                if(offset.contains("/")) {
                    offset = offset.substring(0, offset.lastIndexOf("/"));
                    doRequestList();
                }
                break;

            default:
                HashMap<String,String> map = (HashMap<String,String>)list.get(i);
                String elem = "/" + map.get("name");
                offset = offset + elem;
                if (map.get("type").equals("file")) {
                    address = "https://" + map.get("address") + ".datacloudmail.ru/weblink/view/" + reference + offset + "?etag=" + map.get("hash") + "&key=" + map.get("token");
                    doDownload();
                }
                else doRequestList();
                break;
        }
    }

    private void doRequestList() {
        new RequestList(this, resource, reference, offset);
    }

    private void doDownload() {
        new Download(this, address);
    }

    @Override
    public void complete(Object o, Object res) throws Exception {
        if (o instanceof RequestList) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: RequestList");
            list = (ArrayList<Map<String, String>>) res;
            showDialog(DIALOG_LIST);
        } else if (o instanceof Download) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: Download");
            tvContent.setText((String) res);
            inflateView((String)res);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_LIST:
                adb.setTitle(list_title + offset);
                ArrayList<String> names = getNames();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, names);
                adb.setAdapter(adapter, this);
                adb.setNeutralButton(R.string.cancel, this);
                adb.setNegativeButton(R.string.back, this);
                return adb.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        AlertDialog aDialog = (AlertDialog) dialog;
        switch (id) {
            case DIALOG_LIST:
                aDialog.setTitle(getResources().getString(R.string.list_title) + offset);
                ArrayList<String> names = getNames();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, names);
                aDialog.getListView().setAdapter(adapter);
                break;
        }
    }

    private ArrayList<String> getNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Map<String, String> i : list) {
            names.add((i).get("name"));
        }
        return names;
    }

    private void inflateView(String path) throws Exception {
        Intent intent = new  Intent(this, ViewActivity.class);
        intent.putExtra("path",path);
        startActivity(intent);
    }
}
