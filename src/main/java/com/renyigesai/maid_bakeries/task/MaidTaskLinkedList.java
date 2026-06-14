package com.renyigesai.maid_bakeries.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.server.level.ServerLevel;

public class MaidTaskLinkedList {
    public MaidTaskNode hand = new MaidTaskNode(null);
    public boolean flag = false;
    public int repeatCount = 1;
    public int count = 0;


    public void add(MaidTaskNode node){
        MaidTaskNode temp = hand;
        while (true){
            if (temp.next == null){
                break;
            }
            temp = temp.next;
        }
        temp.next = node;
        node.pre = temp;
    }

    public MaidTaskNode currentTaskNode(){
        MaidTaskNode current = hand.next;
        while (current != null) {
            if (!current.pass) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public void completeCurrentNode() {
        MaidTaskNode current = currentTaskNode();
        if (current == null) {
            return;
        }
        current.pass = true;
//        if (current.next == null) {
//            count++;
//            if (count < repeatCount) {
//                MaidTaskNode node = hand.next;
//                while (node != null) {
//                    node.pass = false;
//                    node = node.next;
//                }
//            }else {
//                hand.next = null;
//            }
//        }
    }

    public void repeatExecution(){
        MaidTaskNode node = hand.next;
        while (true){
            if (node == null){
                break;
            }
            node.pass = false;
            node = node.next;
        }
    }
    public void refreshAllPass(ServerLevel level, EntityMaid maid) {
        refreshPass(level,maid);
        MaidTaskNode last = hand.next;
        if (last == null){
            return;
        }
        while (last.next != null) {
            last = last.next;
        }
        boolean needPropagate = false;
        MaidTaskNode current = last;
        while (current != null && current != hand) {
            if (current.pass) {
                needPropagate = true;
            }
            if (needPropagate) {
                current.pass = true;
            }
            current = current.pre;
        }
    }

    public void refreshPass(ServerLevel level, EntityMaid maid) {
        MaidTaskNode last = hand.next;
        if (last == null){
            return;
        }
        while (last.next != null) {
            if (last.task.successFlag(level, maid)) {
                last.pass = true;
            }
            last = last.next;
        }
    }
}
