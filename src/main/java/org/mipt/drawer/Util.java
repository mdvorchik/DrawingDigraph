package org.mipt.drawer;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static <T> List<T> listFromIterable(Iterable<? extends T> iterable) {
        List<T> myList = new ArrayList<>();
        for (T t : iterable) {
            myList.add(t);
        }
        return myList;
    }
}
