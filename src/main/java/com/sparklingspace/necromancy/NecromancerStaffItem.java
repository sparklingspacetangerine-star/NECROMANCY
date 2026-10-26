package com.sparklingspace.necromancy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class NecromancerStaffItem extends Item {
    public NecromancerStaffItem(Settings settings) { super(settings); }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack=player.getStackInHand(hand);
        if (!world.isClient && player instanceof ServerPlayerEntity p) {
            if (player.isSneaking()) NecromancyMod.cycleStaff(p); else NecromancyMod.useStaff(p);
            stack.damage(1, p, x -> x.sendToolBreakStatus(hand));
        }
        return TypedActionResult.success(stack, world.isClient());
    }
    @Override public boolean isItemBarVisible(ItemStack stack) { return true; }
    @Override public int getItemBarStep(ItemStack stack) { return Math.round(13f * (1f - stack.getDamage() / (float) stack.getMaxDamage())); }
    @Override public int getItemBarColor(ItemStack stack) { return 0x7a1fa2; }
}
