package com.merdeev.experiment;

import java.io.Serializable;

/**
 * Упаковывает данные для передачи между Activity
 * @author R.Z.Merdeev
 */
class Ser implements Serializable {
    /** Ресурс данных */
    final Object resource;

    /** Тип данных */
    final Class type;

    /**
     * Коструктор,
     * сохраняет параметры
     * @param resource ресурс данных
     * @param type тип данных
     */
    Ser(Object resource, Class type) {
        // Сохраняются переданные параметры
        this.resource = resource;
        this.type = type;
    }
}
