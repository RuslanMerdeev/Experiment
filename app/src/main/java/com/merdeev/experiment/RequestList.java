package com.merdeev.experiment;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by r.merdeev on 02.10.2017.
 */

public class RequestList extends AsyncTask {
    private String resource;
    private String reference;
    private String offset;
    private ArrayList<Map<String, String>> list;
    private CompleteListener cl;
    private String token;
    private String address;

    RequestList(CompleteListener cl, String resource, String reference, String offset) {
        super();
        if (resource.contains("mail.ru") == false) return;
        this.resource = resource;
        this.reference = reference;
        this.offset = offset;
        this.cl = cl;
        list = new ArrayList<>();

        this.execute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(MainActivity.LOG, "requestList: doInBackground");
        try {
            Connection con = Jsoup.connect(resource+reference+offset);

            Document doc = con.get();
            String head = doc.head().toString();
            token = findToken(head);
            address = findView(head);
            String body = doc.body().toString();
            body = body.substring(body.lastIndexOf("\"list\"")+6,body.lastIndexOf("\"id\""));
            ArrayList<String> divList = splitList(body);
            list = makeMapList(divList);

            Log.d(MainActivity.LOG, ((HashMap<String, String>)list.get(5)).get("address"));
        }
        catch (Exception e) {
            Log.d(MainActivity.LOG, "requestList: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        cl.complete(this, list);
    }

    ArrayList<String> splitList(String list) throws Exception {
        ArrayList<String> al = new ArrayList<>();
        list = list.substring(list.indexOf("\""), list.lastIndexOf("\"") + 1);
        list = list.replaceAll("\t", "");
        String[] temp = list.split("\n\\},\n\\{\n");
        for (String i : temp) al.add(i);
        return al;
    }

    ArrayList<Map<String, String>> makeMapList(ArrayList<String> list) throws Exception {
        ArrayList<Map<String, String>> mapArrayList = new ArrayList<>();
        String temp;
        for (String i : list) {
            HashMap<String, String> hashMap = new HashMap<>();
            temp = i.substring(i.indexOf("\"type\"")+9);
            String type = temp.substring(0, temp.indexOf("\""));
            hashMap.put("type", type);
            temp = i.substring(i.indexOf("\"name\"")+9);
            String name = temp.substring(0, temp.indexOf("\""));
            hashMap.put("name", name);
            if (type.equals("file")) {
                temp = i.substring(i.indexOf("\"hash\"")+9);
                hashMap.put("hash", temp.substring(0, temp.indexOf("\"")));
                hashMap.put("token", token);
                hashMap.put("address", address);
            }

            mapArrayList.add(hashMap);
        }
        return mapArrayList;
    }

    String findToken(String text) throws Exception {
        String token;
        token = text.substring(text.lastIndexOf("\"tokens\""));
        token = token.substring(token.indexOf("\"download\"")+13);
        token = token.substring(0, token.indexOf("\""));
        return token;
    }

    String findView(String text) throws Exception {
        String address;
        address = text.substring(0,text.indexOf(".datacloudmail.ru/view/"));
        address = address.substring(address.lastIndexOf("//")+2);
        return address;
    }
}
