package net.createdemolition.entity;

import net.createdemolition.blast.DemolitionBlast;
import net.createdemolition.registry.ModEntities;
import net.createdemolition.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Fitili yanan dinamit çubuğu: sekiyor, fitil bitince patlıyor. */
public class DynamiteEntity extends ThrowableItemProjectile {
    public static final float POWER = 3.0f;
    private int fuse = 70;

    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level level) { super(type, level); }

    public DynamiteEntity(Level level, LivingEntity thrower) { super(ModEntities.DYNAMITE.get(), thrower, level); }

    @Override protected Item getDefaultItem() { return ModItems.DYNAMITE.get(); }

    @Override protected double getDefaultGravity() { return 0.05; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.25, getZ(), 0, 0.02, 0);
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.25, getZ(), 0, 0.01, 0);
        } else {
            if (fuse % 10 == 0) level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.15f, 2.0f);
            if (isInWater() && random.nextInt(4) == 0) fuse++; // ıslak fitil yavaş yanar
            if (--fuse <= 0) {
                discard();
                DemolitionBlast.detonate((ServerLevel) level(), position(), POWER, false, getOwner());
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Vec3 v = getDeltaMovement();
        Direction.Axis axis = hit.getDirection().getAxis();
        Vec3 bounced = new Vec3(
                axis == Direction.Axis.X ? -v.x : v.x,
                axis == Direction.Axis.Y ? -v.y : v.y,
                axis == Direction.Axis.Z ? -v.z : v.z).scale(0.3);
        Vec3 n = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        setPos(hit.getLocation().add(n.scale(0.13)));
        setDeltaMovement(bounced.lengthSqr() < 0.002 ? Vec3.ZERO : bounced);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        setDeltaMovement(getDeltaMovement().scale(-0.1));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuse);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getInt("Fuse");
    }
}
