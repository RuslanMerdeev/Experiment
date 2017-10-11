package com.merdeev.experiment;

import java.io.Serializable;

/**
 * Упаковывает данные для передачи между Activity
 * @author R.Z.Merdeev
 */
public class Ser implements Serializable {
    /** Ресурс данных */
    Object resource;

    /** Тип данных */
    Class type;

    /**
     * Коструктор,
     * сохраняет параметры
     * @param resource ресурс данных
     * @param type тип данных
     */
    public Ser(Object resource, Class type) {
        // Сохраняются переданные параметры
        this.resource = resource;
        this.type = type;
    }
}
