package com.sparklingspace.necromancy;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class CommandMenuScreen extends Screen {
 protected CommandMenuScreen(){super(Component.literal("Undead Commands"));}
 @Override protected void init(){int x=width/2-100; addRenderableWidget(Button.builder(Component.literal("Follow Player"),b->send("follow")).bounds(x,45,200,20).build()); addRenderableWidget(Button.builder(Component.literal("Roam Freely"),b->send("roam")).bounds(x,72,200,20).build()); addRenderableWidget(Button.builder(Component.literal("Defend Position"),b->send("defend")).bounds(x,99,200,20).build());}
 private void send(String c){if(minecraft!=null&&minecraft.player!=null)minecraft.player.connection.sendCommand("necromancy command "+c);onClose();}
 @Override public boolean isPauseScreen(){return false;}
}
