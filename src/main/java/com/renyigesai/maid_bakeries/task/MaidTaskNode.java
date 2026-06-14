package com.renyigesai.maid_bakeries.task;

public class MaidTaskNode {
    public MaidTaskNode next;
    public MaidTaskNode pre;
    public AbstractCraftMaidTask task;
    public boolean pass = false;

    public MaidTaskNode(AbstractCraftMaidTask task) {
        this.task = task;
    }

}
