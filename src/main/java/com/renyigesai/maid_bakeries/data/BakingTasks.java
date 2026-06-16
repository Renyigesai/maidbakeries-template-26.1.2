package com.renyigesai.maid_bakeries.data;

import com.renyigesai.maid_bakeries.entity.task.MaidTaskLinkedList;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BakingTasks {
    public static ConcurrentHashMap<UUID, MaidTaskLinkedList> map;
    static {
        map = new ConcurrentHashMap<>();
    }

    public static void add(UUID key, MaidTaskLinkedList linkedList){
        map.put(key,linkedList);
        System.out.println(map);
    }

    public static void remove(UUID key) {
        map.remove(key);
    }
}
