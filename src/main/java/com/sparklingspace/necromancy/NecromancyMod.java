package com.sparklingspace.necromancy;

import net.fabricmc.api.ModInitializer;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import static net.minecraft.server.command.CommandManager.literal;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.projectile.ProjectileUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NecromancyMod implements ModInitializer {
    public static final String MOD_ID = "necromancy";
    public static final Item BOOK_OF_SCOURGE = new Item(new Item.Settings().maxCount(1));
    public static final Item NECROMANCER_STAFF = new NecromancerStaffItem(new Item.Settings().maxCount(1).maxDamage(512));
    private static final Map<UUID, Integer> ENERGY = new HashMap<>();
    private static final Map<UUID, Integer> SUMMON_MODE = new HashMap<>();
    private static final Map<UUID, Integer> STAFF_MODE = new HashMap<>();
    public static Identifier id(String path) { return new Identifier(MOD_ID, path); }
    public static int energy(ServerPlayerEntity p) { return ENERGY.getOrDefault(p.getUuid(), 100); }
    public static boolean unlocked(ServerPlayerEntity p) { return p.getCommandTags().contains(MOD_ID + ":unlocked"); }
    public static int staffMode(ServerPlayerEntity p) { return STAFF_MODE.getOrDefault(p.getUuid(), 0); }
    public static int summonMode(ServerPlayerEntity p) { return SUMMON_MODE.getOrDefault(p.getUuid(), 0); }
    public static boolean spend(ServerPlayerEntity p, int amount) {
        if (!unlocked(p) || energy(p) < amount) return false;
        ENERGY.put(p.getUuid(), energy(p) - amount); return true;
    }
    @Override public void onInitialize() {
        net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.ITEM, id("book_of_scourge"), BOOK_OF_SCOURGE);
        net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.ITEM, id("necromancer_staff"), NECROMANCER_STAFF);
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(BOOK_OF_SCOURGE)) return TypedActionResult.pass(stack);
            if (!world.isClient && player instanceof ServerPlayerEntity p) {
                p.getCommandTags().add(MOD_ID + ":unlocked"); ENERGY.putIfAbsent(p.getUuid(), 100);
                p.sendMessage(Text.literal("§5The Book of the Scourge awakens your necromantic power."), true);
            }
            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return;
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) if (unlocked(p) && energy(p) < 100) ENERGY.put(p.getUuid(), energy(p) + 1);
        });
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.CLERIC, 1, factories -> factories.add((entity, random) -> new TradeOffer(new ItemStack(Items.EMERALD, 12), new ItemStack(BOOK_OF_SCOURGE), 1, 5, 0.05f)));
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> registerCommands(dispatcher));
    }
    private static void registerCommands(CommandDispatcher<ServerCommandSource> d) {
        d.register(literal("necromancy").then(literal("menu").executes(c -> {
            ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
            p.sendMessage(Text.literal("§5Necromancer menu: sneak-right-click staff = Battle/Summon. Use /necromancy summon warrior|archer|worker."), false); return 1;
        })).then(literal("summon").then(literal("warrior").executes(c -> selectSummon(c.getSource().getPlayerOrThrow(), 0))).then(literal("archer").executes(c -> selectSummon(c.getSource().getPlayerOrThrow(), 1))).then(literal("worker").executes(c -> selectSummon(c.getSource().getPlayerOrThrow(), 2))));
    }
    private static int selectSummon(ServerPlayerEntity p, int mode) { SUMMON_MODE.put(p.getUuid(), mode); p.sendMessage(Text.literal("§5Summon selection: " + summonName(mode)), true); return 1; }

    public static void cycleStaff(ServerPlayerEntity p) { int m=(staffMode(p)+1)%2; STAFF_MODE.put(p.getUuid(),m); p.sendMessage(Text.literal("§5Staff mode: " + (m==0?"Battle":"Summon")),true); }
    public static void cycleSummon(ServerPlayerEntity p) { int m=(summonMode(p)+1)%3; SUMMON_MODE.put(p.getUuid(),m); p.sendMessage(Text.literal("§5Summon: "+summonName(m)),true); }
    public static String summonName(int mode) { return new String[]{"Skeleton Warrior","Skeleton Archer","Skeleton Worker"}[mode]; }
    public static void useStaff(ServerPlayerEntity p) { if (!unlocked(p)) { p.sendMessage(Text.literal("§8Read the Book of the Scourge first."),true); return; } if(staffMode(p)==0) battle(p); else summon(p); }
    private static void battle(ServerPlayerEntity p) {
        if (!spend(p,8)) { p.sendMessage(Text.literal("§8Not enough Dark Energy."),true); return; }
        Vec3d start=p.getCameraPosVec(1f), end=start.add(p.getRotationVec(1f).multiply(24));
        EntityHitResult hit=ProjectileUtil.raycast(p,start,end,new Box(start,end),e->e.isAlive()&&!e.isSpectator(),576);
        if(hit!=null) { hit.getEntity().damage(p.getDamageSources().magic(),8); p.sendMessage(Text.literal("§dDark bolt strikes its target."),true); }
        else p.sendMessage(Text.literal("§dDark bolt dissipates."),true);
    }
    private static void summon(ServerPlayerEntity p) {
        if(!spend(p,25)){p.sendMessage(Text.literal("§8Not enough Dark Energy."),true);return;}
        if(summonMode(p)==2){ ZombieEntity z=EntityType.ZOMBIE.create(p.getServerWorld()); if(z==null)return; z.refreshPositionAndAngles(p.getX()+1,p.getY(),p.getZ()+1,p.getYaw(),0); z.setCustomName(Text.literal("Skeleton Worker")); z.setCustomNameVisible(true); z.addCommandTag(MOD_ID+":worker:"+p.getUuid()); z.equipStack(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_PICKAXE)); p.getServerWorld().spawnEntity(z); }
        else { SkeletonEntity s=EntityType.SKELETON.create(p.getServerWorld()); if(s==null)return; s.refreshPositionAndAngles(p.getX()+1,p.getY(),p.getZ()+1,p.getYaw(),0); s.setCustomName(Text.literal(summonName(summonMode(p)))); s.setCustomNameVisible(true); s.addCommandTag(MOD_ID+":minion:"+p.getUuid()); if(summonMode(p)==0)s.equipStack(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD)); p.getServerWorld().spawnEntity(s); }
        p.sendMessage(Text.literal("§5"+summonName(summonMode(p))+" rises from the grave."),true);
    }
}
