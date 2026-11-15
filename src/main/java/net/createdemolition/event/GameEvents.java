package net.createdemolition.event;

import net.createdemolition.CreateDemolition;
import net.createdemolition.blast.DemolitionBlast;
import net.createdemolition.item.Unstable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = CreateDemolition.MODID)
public class GameEvents {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel sl) DemolitionBlast.tickQueue(sl);
    }

    /** Kararsız madde taşıyan oyuncu yanarsa, yüksekten düşerse veya patlamaya yakalanırsa... */
    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || !(player.level() instanceof ServerLevel level)) return;
        DamageSource src = event.getSource();
        float dmg = event.getNewDamage();

        float chance;
        if (src.is(DamageTypeTags.IS_EXPLOSION)) chance = 0.9f;
        else if (src.is(DamageTypeTags.IS_FIRE)) chance = 0.3f;
        else if (src.is(DamageTypeTags.IS_FALL)) chance = Math.min(0.12f * dmg, 0.9f);
        else if (src.is(DamageTypeTags.IS_PROJECTILE)) chance = 0.35f;
        else if (src.is(DamageTypeTags.IS_LIGHTNING)) chance = 1.0f;
        else return;

        float total = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof Unstable u) total += u.volatility() * s.getCount();
        }
        if (total <= 0 || level.random.nextFloat() >= chance) return;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() instanceof Unstable) inv.setItem(i, ItemStack.EMPTY);
        }
        player.displayClientMessage(Component.translatable("message.createdemolition.cargo_detonated"), true);
        float power = Math.min(1.5f + total * 0.3f, 8.0f);
        DemolitionBlast.schedule(level, player.position().add(0, 0.5, 0), power, true, 1);
    }

    /** Yere atılmış kararsız madde ateşe veya lava değerse infilak eder. */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item) || !(item.level() instanceof ServerLevel level)) return;
        ItemStack stack = item.getItem();
        if (stack.getItem() instanceof Unstable u && (item.isOnFire() || item.isInLava())) {
            float power = Math.min(1.5f + u.volatility() * stack.getCount() * 0.3f, 8.0f);
            item.discard();
            DemolitionBlast.detonate(level, item.position(), power, true, null);
        }
    }
}
