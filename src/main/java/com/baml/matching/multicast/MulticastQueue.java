package com.baml.matching.multicast;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class MulticastQueue {

    private static List<String> list = new ArrayList<>();

    private List<Subs> subsList = new ArrayList<>();

    public void register(Subs sub) {
        sub.offset = 0;
    }

    public MulticastQueue(int size) {
    }

    public static class Subs {
        int offset;
        public String poll() {
            return list.get(offset++);
        }
    }


//    public String poll(Consumer<Subs> consumer) {
//        consumer.accept(consumer);
//    }

}

