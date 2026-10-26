package com.sparklingspace.necromancy;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SummonMenuScreen extends Screen {
    protected SummonMenuScreen() { super(Component.literal("Necromancer Summons")); }
    @Override protected void init() {
        int x = width / 2 - 100;
        addRenderableWidget(Button.builder(Component.literal("Skeleton Warrior"), b -> choose("warrior")).bounds(x, 45, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Skeleton Archer"), b -> choose("archer")).bounds(x, 72, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Skeleton Worker"), b -> { if(minecraft!=null) minecraft.setScreen(new WorkerMenuScreen()); }).bounds(x, 99, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Undead Commands"), b -> { if(minecraft!=null) minecraft.setScreen(new CommandMenuScreen()); }).bounds(x, 126, 200, 20).build());
    }
    private void choose(String type) {
        if (minecraft != null && minecraft.player != null) minecraft.player.connection.sendCommand("necromancy summon " + type);
        onClose();
    }
    @Override public boolean isPauseScreen() { return false; }
}
