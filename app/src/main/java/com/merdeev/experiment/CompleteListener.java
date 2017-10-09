package com.merdeev.experiment;

/**
 * Ожидание уведомления о завершении выполнения задачи,
 * необходимо для запуска последовательности задач
 * @author R.Z.Merdeev
 */
public interface CompleteListener {

    /**
     * При завершении выполнения задачи
     * @param o источник вызова, объект класса
     * @param res результат, произвольные данные
     * @throws Exception
     */
    void complete (Object o, Object res) throws Exception;
}
