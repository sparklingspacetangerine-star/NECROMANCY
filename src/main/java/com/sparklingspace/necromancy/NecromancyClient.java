package com.sparklingspace.necromancy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class NecromancyClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> renderEnergy(context));
    }
    private static void renderEnergy(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.player.getCommandTags().contains(NecromancyMod.MOD_ID + ":unlocked")) return;
        int x = 8, y = context.getScaledWindowHeight() - 34, width = 120;
        context.fill(x, y, x + width, y + 10, 0xAA24152E);
        context.fill(x + 1, y + 1, x + width - 1, y + 9, 0xFF7A1FA2);
        context.drawTextWithShadow(client.textRenderer, Text.literal("Dark Energy 100/100"), x, y - 11, 0xE8B7FF);
    }
}
