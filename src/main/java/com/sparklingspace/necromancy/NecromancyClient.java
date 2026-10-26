package com.sparklingspace.necromancy;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NecromancyMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NecromancyClient {
    private NecromancyClient() {}
    @SubscribeEvent public static void renderEnergy(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getTags().contains(NecromancyMod.MOD_ID + ":unlocked")) return;
        int x = 8, y = event.getWindow().getGuiScaledHeight() - 34, width = 120;
        event.getGuiGraphics().fill(x, y, x + width, y + 10, 0xAA24152E);
        event.getGuiGraphics().fill(x + 1, y + 1, x + width - 1, y + 9, 0xFF7A1FA2);
        event.getGuiGraphics().drawString(mc.font, Component.literal("Dark Energy 100/100"), x, y - 11, 0xE8B7FF, true);
    }
}
