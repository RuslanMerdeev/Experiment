package com.merdeev.experiment;

import android.app.Dialog;
import android.content.DialogInterface;
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


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, CompleteListener {

    final static String LOG = "States";
    TextView tvContent;
    ArrayList<String> list;
    String offset = "";
    final int DIALOG_LIST = 1;
    String list_title;
    String resource;
    String reference;

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
                String elem = list.get(i);
                offset = offset + elem;
                if (elem.contains(".")) doDownload();
                else doRequestList();
                break;
        }
    }

    void doRequestList() {
        new RequestList(this, resource, reference, offset);
    }

    void doDownload() {
        new Download(this, "https://cloclo44.datacloudmail.ru/weblink/view/6mMo/hyo9wksZC/andro.gif?etag=5A43A9D24CE4EB0A8FF3679ED63C0B948B26EA9C&key=174ab43770e48095b1c389cbeb048e2432c83513");
    }

    @Override
    public void complete(Object o, Object res) {
        if (o instanceof RequestList) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: RequestList");
            list = (ArrayList<String>) res;
            showDialog(DIALOG_LIST);
        } else if (o instanceof Download) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: Download");
            tvContent.setText((String) res);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_LIST:
                adb.setTitle(list_title + offset);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, list);
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, list);
                aDialog.getListView().setAdapter(adapter);
                break;
        }
    }
}
