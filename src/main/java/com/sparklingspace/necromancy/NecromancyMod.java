package com.sparklingspace.necromancy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.sounds.SoundEvents;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(NecromancyMod.MOD_ID)
public class NecromancyMod {
 public static final String MOD_ID="necromancy";
 public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MOD_ID);
 public static final DeferredRegister<PoiType> POI_TYPES=DeferredRegister.create(ForgeRegistries.POI_TYPES,MOD_ID);
 public static final DeferredRegister<VillagerProfession> PROFESSIONS=DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS,MOD_ID);
 public static final RegistryObject<PoiType> ACOLYTE_POI=POI_TYPES.register("acolyte",()->new PoiType(java.util.Set.of(Blocks.SOUL_SAND.defaultBlockState()),1,1));
 public static final RegistryObject<VillagerProfession> ACOLYTE=PROFESSIONS.register("acolyte",()->new VillagerProfession("acolyte",h->h.value()==ACOLYTE_POI.get(),h->h.value()==ACOLYTE_POI.get(),ImmutableSet.of(),ImmutableSet.of(Blocks.SOUL_SAND),SoundEvents.VILLAGER_WORK_CLERIC));
 public static final RegistryObject<Item> BOOK_OF_SCOURGE=ITEMS.register("book_of_scourge",()->new Item(new Item.Properties().stacksTo(1)));
 public static final RegistryObject<Item> NECROMANCER_STAFF=ITEMS.register("necromancer_staff",()->new NecromancerStaffItem(new Item.Properties().stacksTo(1).durability(512)));
 private static final Map<UUID,Integer> ENERGY=new HashMap<>(), SUMMON_MODE=new HashMap<>(), STAFF_MODE=new HashMap<>();
 public NecromancyMod(){IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();ITEMS.register(bus);POI_TYPES.register(bus);PROFESSIONS.register(bus);MinecraftForge.EVENT_BUS.register(this);}
 public static int energy(ServerPlayer p){return ENERGY.getOrDefault(p.getUUID(),100);}
 public static boolean unlocked(ServerPlayer p){return p.getTags().contains(MOD_ID+":unlocked");}
 public static int staffMode(ServerPlayer p){return STAFF_MODE.getOrDefault(p.getUUID(),0);}
 public static int summonMode(ServerPlayer p){return SUMMON_MODE.getOrDefault(p.getUUID(),0);}
 public static boolean spend(ServerPlayer p,int n){if(!unlocked(p)||energy(p)<n)return false;ENERGY.put(p.getUUID(),energy(p)-n);return true;}
 @SubscribeEvent public void rightClick(PlayerInteractEvent.RightClickItem e){if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItemStack().is(BOOK_OF_SCOURGE.get()))return;p.addTag(MOD_ID+":unlocked");ENERGY.putIfAbsent(p.getUUID(),100);p.displayClientMessage(Component.literal("§5The Book of the Scourge awakens your necromantic power."),true);e.setCanceled(true);}
 @SubscribeEvent public void serverTick(TickEvent.ServerTickEvent e){if(e.phase!=TickEvent.Phase.END||e.getServer().getTickCount()%20!=0)return;for(ServerPlayer p:e.getServer().getPlayerList().getPlayers())if(unlocked(p)&&energy(p)<100)ENERGY.put(p.getUUID(),energy(p)+1);}
 @SubscribeEvent public void trades(VillagerTradesEvent e){if(e.getType()==ACOLYTE.get())e.getTrades().get(1).add((t,r)->new MerchantOffer(new ItemStack(Items.EMERALD,12),new ItemStack(BOOK_OF_SCOURGE.get()),1,5,.05f));}
 @SubscribeEvent public void protectTargets(LivingChangeTargetEvent e){
  LivingEntity ownerGuard=e.getEntity();
  if(!(ownerGuard instanceof net.minecraft.world.entity.Mob mob)||!hasMinionTag(mob)||e.getNewTarget()==null)return;
  if(e.getNewTarget() instanceof net.minecraft.world.entity.player.Player||e.getNewTarget() instanceof Villager)e.setNewTarget(null);
 }
 private static boolean hasMinionTag(net.minecraft.world.entity.Mob mob){for(String tag:mob.getTags())if(tag.startsWith(MOD_ID+":minion:")||tag.startsWith(MOD_ID+":worker:"))return true;return false;}
 private static void addHostileGoal(net.minecraft.world.entity.Mob mob){mob.targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(mob,Monster.class,true));}
 @SubscribeEvent public void commands(RegisterCommandsEvent e) {
  CommandDispatcher<CommandSourceStack> d=e.getDispatcher();
  d.register(Commands.literal("necromancy")
   .then(Commands.literal("menu").executes(c -> {
    c.getSource().getPlayerOrException().displayClientMessage(Component.literal("§5Sneak-right-click staff: Battle/Summon. Use /necromancy summon warrior|archer|worker."), false);
    return 1;
   }))
   .then(Commands.literal("summon")
    .then(Commands.literal("warrior").executes(c -> select(c.getSource().getPlayerOrException(), 0)))
    .then(Commands.literal("archer").executes(c -> select(c.getSource().getPlayerOrException(), 1)))
    .then(Commands.literal("worker").executes(c -> select(c.getSource().getPlayerOrException(), 2)))));
 }

 private static int select(ServerPlayer p,int m){SUMMON_MODE.put(p.getUUID(),m);p.displayClientMessage(Component.literal("§5Summon selection: "+summonName(m)),true);return 1;}
 public static void cycleStaff(ServerPlayer p){int m=(staffMode(p)+1)%2;STAFF_MODE.put(p.getUUID(),m);p.displayClientMessage(Component.literal("§5Staff mode: "+(m==0?"Battle":"Summon")),true);}
 public static String summonName(int m){return new String[]{"Skeleton Warrior","Skeleton Archer","Skeleton Worker"}[m];}
 public static void useStaff(ServerPlayer p){if(!unlocked(p)){p.displayClientMessage(Component.literal("§8Read the Book of the Scourge first."),true);return;}if(staffMode(p)==0)battle(p);else summon(p);}
 private static void battle(ServerPlayer p){if(!spend(p,8)){p.displayClientMessage(Component.literal("§8Not enough Dark Energy."),true);return;}Vec3 a=p.getEyePosition(1.0f),b=a.add(p.getViewVector(1).scale(24));EntityHitResult h=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(p,a,b,new AABB(a,b),x->x.isAlive()&&!x.isSpectator(),576);if(h!=null)h.getEntity().hurt(p.damageSources().magic(),8);p.displayClientMessage(Component.literal("§dDark bolt cast."),true);}
 private static void summon(ServerPlayer p){if(!spend(p,25)){p.displayClientMessage(Component.literal("§8Not enough Dark Energy."),true);return;}ServerLevel w=p.serverLevel();if(summonMode(p)==2){Zombie z=EntityType.ZOMBIE.create(w);if(z==null)return;z.moveTo(p.getX()+1,p.getY(),p.getZ()+1,p.getYRot(),0);z.setCustomName(Component.literal("Skeleton Worker"));z.setCustomNameVisible(true);z.addTag(MOD_ID+":worker:"+p.getUUID());z.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_PICKAXE));addHostileGoal(z);w.addFreshEntity(z);}else{Skeleton s=EntityType.SKELETON.create(w);if(s==null)return;s.moveTo(p.getX()+1,p.getY(),p.getZ()+1,p.getYRot(),0);s.setCustomName(Component.literal(summonName(summonMode(p))));s.setCustomNameVisible(true);s.addTag(MOD_ID+":minion:"+p.getUUID());if(summonMode(p)==0)s.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));else s.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));addHostileGoal(s);w.addFreshEntity(s);}p.displayClientMessage(Component.literal("§5"+summonName(summonMode(p))+" rises from the grave."),true);}
}
