package com.sparklingspace.necromancy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public class NecromancerStaffItem extends Item{
 public NecromancerStaffItem(Properties p){super(p);}
 @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){ItemStack s=p.getItemInHand(h);if(!l.isClientSide&&p instanceof ServerPlayer sp){if(p.isCrouching())NecromancyMod.cycleStaff(sp);else NecromancyMod.useStaff(sp);s.hurtAndBreak(1,sp,x->x.broadcastBreakEvent(h));}return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
 @Override public boolean isBarVisible(ItemStack s){return true;}
 @Override public int getBarColor(ItemStack s){return 0x7a1fa2;}
}
