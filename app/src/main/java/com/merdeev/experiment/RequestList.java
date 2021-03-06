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
 * Nнициирует запрос на сервер облака для составления списка данных файлов/папок директории
 * @author R.Z.Merdeev
 */
class RequestList extends AsyncTask {
    /** Данные из ресурсов */
    private String resource;
    private String reference;

    /** Смещение для текущей директории относительно корневой */
    private String offset;

    /** Результат работы */
    private Object result;

    /** Слушатель завершения задачи */
    private CompleteListener cl;

    /**
     * Конструктор,
     * сохраняет параметры и запускает асинхронную задачу
     * @param cl слушатель завершения
     * @param resource ресурс для запроса
     * @param reference ссылка для запроса
     * @param offset смещение для запроса
     */
    RequestList(CompleteListener cl, String resource, String reference, String offset) {
        Trace.save("requestList: constructor");

        // Проверяется, тот ли это ресурс, который ожидаем
        if (!resource.contains("mail.ru")) {
            Trace.save("requestList: constructor: wrong resource");
            return;
        }

        // Сохраняются переданные параметры
        this.resource = resource;
        this.reference = reference;
        this.offset = offset;
        this.cl = cl;

        // Запускается асинхронная задача
        if (this.execute() == null) {
            Trace.save("requestList: constructor: can't execute AsyncTask");
        }
    }

    /**
     * Выполняет фоновую работу по запросу и составлению списка
     * @param objects массив объектов
     * @return объект
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        Trace.save("requestList: doInBackground");
        try {
            // Открывается соединение
            Connection con = Jsoup.connect(resource+reference+offset);

            // Сохраняется ответ
            Document doc = con.get();

            // Nзвлекается заголовок
            String head = doc.head().toString();

            // Nз заголовка извлекается token и address
            String token = findToken(head);
            String address = findAddress(head);

            // Nзвлекается тело
            String body = doc.body().toString();

            // Находится место, где содержатся сырые данные файлов/папок директории облака
            body = body.substring(body.lastIndexOf("\"list\"")+6,body.lastIndexOf("\"id\""));

            // Составляется простой список элементов из сырых данных
            ArrayList<String> divList = splitList(body);

            // Парсятся нужные поля каждого элемента списка
            result = makeMapList(divList, token, address);
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Trace.save("requestList: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Trace.save(i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
        return null;
    }

    /**
     * Уведомляет слушателя об окончании выполнения задачи и отдает результат
     * @param o объект
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        // Уведомляется слушатель о завершении задачи, передается результат
        cl.complete(this, result, ArrayList.class);
    }

    /**
     * Составляет простой список всех элементов директории облака из сырых данных
     * @param text сырые данные
     * @return простой список
     * @throws Exception исключение
     */
    private ArrayList<String> splitList(String text) throws Exception {
        Trace.save("requestList: splitList");

        // Создается список для элементов
        ArrayList<String> al = new ArrayList<>();

        // Проверяется, отсутствуют ли в сырых данных кавычки для определения пустого списка
        if(!text.contains("\"")) return al;

        // Отбрасываются ненужные символы в конце и в начале данных
        text = text.substring(text.indexOf("\""), text.lastIndexOf("\"") + 1);

        // Убираются все TAB
        text = text.replaceAll("\t", "");

        // Данные разделяются на массив с разделителем, характерным для пространства между элементами
        String[] temp = text.split("\n\\},\n\\{\n");

        // Данные массив добавляются в список и возвращается результат
        for (String i : temp) al.add(i);
        return al;
    }

    /**
     * Парсит поля с типом и именем для папок, и поля с типом, именем и hash для файлов;
     * для файлов также сохраняются token и address
     * @param list список элементов
     * @param token текущий token
     * @param address адрес ресурсов для предпросмотра
     * @return список данных
     * @throws Exception исключение
     */
    private ArrayList<Map<String, String>> makeMapList(ArrayList<String> list, String token, String address) throws Exception {
        Trace.save("requestList: makeMapList");

        // Создается список данных для элементов
        ArrayList<Map<String, String>> mapArrayList = new ArrayList<>();

        // Создается переменная хранения временного результата
        String temp;

        // Для каждого элемента простого списка
        for (String i : list) {
            // Создается карта данных элемента
            HashMap<String, String> hashMap = new HashMap<>();

            // Находится информация о типе элемента и сохраняется в карте данных элемента
            temp = i.substring(i.indexOf("\"type\"")+9);
            String type = temp.substring(0, temp.indexOf("\""));
            hashMap.put("type", type);

            // Находится информация об имени элемента и сохраняется в карте данных элемента
            temp = i.substring(i.indexOf("\"name\"")+9);
            String name = temp.substring(0, temp.indexOf("\""));
            hashMap.put("name", name);

            // Проверяется, что тип элемента - файл
            if (type.equals("file")) {
                // Находится информация о hash элемента и сохраняется в карте данных элемента
                temp = i.substring(i.indexOf("\"hash\"")+9);
                hashMap.put("hash", temp.substring(0, temp.indexOf("\"")));

                // Token и address сохраняются в карте данных элемента
                hashMap.put("token", token);
                hashMap.put("address", address);
            }

            // Карта добавляется в список данных
            mapArrayList.add(hashMap);
        }
        return mapArrayList;
    }

    /**
     * Находит и возвращает token из сырых данных
     * @param text сырые данные
     * @return token
     * @throws Exception исключение
     */
    private String findToken(String text) throws Exception {
        String token;
        token = text.substring(text.lastIndexOf("\"tokens\""));
        token = token.substring(token.indexOf("\"download\"")+13);
        token = token.substring(0, token.indexOf("\""));
        return token;
    }

    /**
     * Находит и возвращает address из сырых данных
     * @param text сырые данные
     * @return address
     * @throws Exception исключение
     */
    private String findAddress(String text) throws Exception {
        String address;
        address = text.substring(0,text.indexOf(".datacloudmail.ru/view/"));
        address = address.substring(address.lastIndexOf("//")+2);
        return address;
    }
}
