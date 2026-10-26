package com.sparklingspace.necromancy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = NecromancyMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NecromancyClient {
    private static final KeyMapping SUMMON_MENU = new KeyMapping("key.necromancy.summon_menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.necromancy");
    private NecromancyClient() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(SUMMON_MENU); }

    @Mod.EventBusSubscriber(modid = NecromancyMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ClientEvents {
        private ClientEvents() {}
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && SUMMON_MENU.consumeClick() && Minecraft.getInstance().player != null) {
                Minecraft.getInstance().setScreen(new SummonMenuScreen());
            }
        }
        @SubscribeEvent
        public static void renderEnergy(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !mc.player.getTags().contains(NecromancyMod.MOD_ID + ":unlocked")) return;
            if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
            int x = (event.getWindow().getGuiScaledWidth() - 182) / 2;
            int y = event.getWindow().getGuiScaledHeight() - 50;
            GuiGraphics g = event.getGuiGraphics();
            g.fill(x, y, x + 182, y + 7, 0xAA24152E);
            g.fill(x + 1, y + 1, x + 181, y + 6, 0xFF7A1FA2);
            g.drawString(mc.font, Component.literal("Dark Energy 100/100"), x, y - 10, 0xE8B7FF, true);
        }
    }

}
