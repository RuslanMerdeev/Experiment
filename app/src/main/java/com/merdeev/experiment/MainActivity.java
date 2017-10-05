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

import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, CompleteListener {

    final static String LOG = "States";
    TextView tvContent;
    ArrayList<String> list;
    String offset = "";
    final int DIALOG_LIST = 1;
    String cont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG, "mainActivity: onCreate");

        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());
        Button btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(this);

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

    void doRequestList() {
        new RequestList(this, getResources().getString(R.string.resource), getResources().getString(R.string.reference) + offset);
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
                if (elem.contains(".")) {
//                    try {
                        //new Download(this, new URL("https://cloclo39.datacloudmail.ru/weblink/thumb/xw0/6mMo/hyo9wksZC/andro.gif"));//getResources().getString(R.string.resource) + getResources().getString(R.string.reference) + offset));//"http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd"
                        new D_temp(this, "https://cloclo44.datacloudmail.ru/weblink/view/6mMo/hyo9wksZC/andro.gif?etag=5A43A9D24CE4EB0A8FF3679ED63C0B948B26EA9C&key=174ab43770e48095b1c389cbeb048e2432c83513");//"https://cloclo4.datacloudmail.ru/weblink/view/6mMo/hyo9wksZC/temp.txt?etag=7275736C616E0000000000000000000000000000&key=174ab43770e48095b1c389cbeb048e2432c83513");//
//                    }
//                    catch (MalformedURLException e) {}
                }
                else {
                    doRequestList();
                }
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_LIST:
                adb.setTitle(getResources().getString(R.string.list_title) + offset);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, list);
                adb.setAdapter(adapter, this);
                adb.setNeutralButton(R.string.cancel, this);
                if (offset.contains("/")) adb.setNegativeButton(R.string.back, this);
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

    @Override
    public void complete(Object o, Object res) {
        if (o instanceof RequestList) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: RequestList");
            list = (ArrayList<String>) res;
            showDialog(DIALOG_LIST);
        } else if (o instanceof Download) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: Download");
            cont = (String) res;
        }
    }
}
