package net.createdemolition.blast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Patlama fiziği: kapalı alan güçlendirmesi, mesafeyle azalan şok dalgası,
 * sağırlık/sersemleme etkileri ve zincirleme (sempatik) patlamalar için gecikmeli kuyruk.
 */
public final class DemolitionBlast {
    private DemolitionBlast() {}

    private record Pending(ServerLevel level, Vec3 pos, float power, boolean fire, int[] ticks) {}
    private static final List<Pending> QUEUE = new ArrayList<>();

    /** Zincirleme patlamalar aynı tick içinde değil, birkaç tick sonra gerçekleşir. */
    public static void schedule(ServerLevel level, Vec3 pos, float power, boolean fire, int delay) {
        QUEUE.add(new Pending(level, pos, power, fire, new int[]{Math.max(1, delay)}));
    }

    public static void tickQueue(ServerLevel level) {
        if (QUEUE.isEmpty()) return;
        List<Pending> ready = new ArrayList<>();
        for (Iterator<Pending> it = QUEUE.iterator(); it.hasNext(); ) {
            Pending p = it.next();
            if (p.level != level) continue;
            if (--p.ticks[0] <= 0) { ready.add(p); it.remove(); }
        }
        for (Pending p : ready) detonate(p.level, p.pos, p.power, p.fire, null);
    }

    /** Etrafı ne kadar kapalıysa patlama o kadar güçlü olur (0..6 kapalı yön). */
    public static int confinement(Level level, BlockPos pos) {
        int closed = 0;
        for (Direction d : Direction.values()) {
            for (int i = 1; i <= 2; i++) {
                BlockState s = level.getBlockState(pos.relative(d, i));
                if (!s.isAir() && s.getFluidState().isEmpty()
                        && s.getExplosionResistance(level, pos, null) >= 1.0f) { closed++; break; }
            }
        }
        return closed;
    }

    public static void detonate(ServerLevel level, Vec3 pos, float basePower, boolean fire, @Nullable Entity source) {
        int closed = confinement(level, BlockPos.containing(pos));
        float power = Math.min(basePower * (1.0f + 0.08f * closed), 16f);

        level.explode(source, pos.x, pos.y, pos.z, power, fire, Level.ExplosionInteraction.TNT);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z,
                (int) (power * 6), power * 0.4, power * 0.3, power * 0.4, 0.02);

        shockwave(level, pos, power, source);
    }

    /** Vanilla patlama yarıçapının ötesine uzanan, mesafeyle zayıflayan şok dalgası. */
    private static void shockwave(ServerLevel level, Vec3 center, float power, @Nullable Entity source) {
        double radius = power * 3.0;
        AABB box = new AABB(center, center).inflate(radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            double dist = e.position().distanceTo(center);
            if (dist > radius || dist < 0.01) continue;
            double strength = 1.0 - dist / radius;
            boolean exposed = level.clip(new ClipContext(center, e.getEyePosition(),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e)).getType() == HitResult.Type.MISS;
            if (!exposed) strength *= 0.35; // siper almak şok dalgasını azaltır

            Vec3 push = e.position().subtract(center).normalize().scale(strength * 1.2);
            e.push(push.x, push.y * 0.5 + strength * 0.3, push.z);
            e.hurtMarked = true;

            if (dist > power * 1.5) { // vanilla hasarının ulaşmadığı bölge
                e.hurt(level.damageSources().explosion(source, source), (float) (strength * power * 1.2));
            }
            int dur = (int) (strength * 240);
            if (dur > 20) {
                e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, dur, 0)); // kulak çınlaması / sersemleme
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur / 2, strength > 0.6 ? 1 : 0));
            }
            if (e instanceof ServerPlayer sp && strength > 0.7) {
                sp.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0)); // gözler kararır
            }
        }
    }
}
