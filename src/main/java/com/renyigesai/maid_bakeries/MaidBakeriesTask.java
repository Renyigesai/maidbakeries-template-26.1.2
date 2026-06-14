package com.renyigesai.maid_bakeries;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.renyigesai.maid_bakeries.task.*;

@LittleMaidExtension
public class MaidBakeriesTask implements ILittleMaid {

    public MaidBakeriesTask() {
    }

    @Override
    public void addMaidTask(TaskManager manager) {
        ILittleMaid.super.addMaidTask(manager);
        manager.add(new TaskCut());
        manager.add(new TaskBaking());
    }
}
