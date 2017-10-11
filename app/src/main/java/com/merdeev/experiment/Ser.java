package com.merdeev.experiment;

import java.io.Serializable;

/**
 * Created by r.merdeev on 10.10.2017.
 */

public class Ser implements Serializable {
    Object res;
    String type;
    public Ser(Object res, String type) {
        this.res = res;
        this.type = type;
    }
}
