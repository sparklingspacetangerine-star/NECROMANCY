package com.sparklingspace.necromancy;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class WorkerMenuScreen extends Screen {
 protected WorkerMenuScreen(){super(Component.literal("Skeleton Worker Orders"));}
 @Override protected void init(){int x=width/2-100; addRenderableWidget(Button.builder(Component.literal("Mine Wood (Axe)"),b->send("wood")).bounds(x,45,200,20).build()); addRenderableWidget(Button.builder(Component.literal("Mine Stone (Pickaxe)"),b->send("stone")).bounds(x,72,200,20).build()); addRenderableWidget(Button.builder(Component.literal("Stop and Store Items"),b->send("stop")).bounds(x,99,200,20).build()); addRenderableWidget(Button.builder(Component.literal("Show Inventory"),b->send("inventory")).bounds(x,126,200,20).build());}
 private void send(String c){if(minecraft!=null&&minecraft.player!=null)minecraft.player.connection.sendCommand("necromancy worker "+c);onClose();}
 @Override public boolean isPauseScreen(){return false;}
}
