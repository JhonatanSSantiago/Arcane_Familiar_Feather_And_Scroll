package com.jhonatan.arcanefamiliar.block;

import com.jhonatan.arcanefamiliar.entity.CourierEntity;
import com.jhonatan.arcanefamiliar.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OwlNestBlockEntity extends BlockEntity {
    private boolean hasEgg = false;
    private int hatchProgress = 0;
    private static final int HATCH_TIME = 2400; // Tempo de incubação em ticks (aprox. 2 minutos)

    public OwlNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OWL_NEST_BE.get(), pos, state);
    }

    public boolean hasEgg() {
        return this.hasEgg;
    }

    public void setHasEgg(boolean hasEgg) {
        this.hasEgg = hasEgg;
        this.hatchProgress = 0;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OwlNestBlockEntity nest) {
        if (level.isClientSide) return;

        if (nest.hasEgg) {
            nest.hatchProgress++;

            // Quando o tempo acaba, eclode o ovo!
            if (nest.hatchProgress >= HATCH_TIME) {
                nest.hatchEgg(level, pos);
            }
        }
    }

    private void hatchEgg(Level level, BlockPos pos) {
        this.hasEgg = false;
        this.hatchProgress = 0;
        this.setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);

        // Cria a coruja bebé em cima do ninho
        CourierEntity babyOwl = ModEntityTypes.COURIER.get().create(level);
        if (babyOwl != null) {
            babyOwl.setBaby(true);
            babyOwl.setPos(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);

            // O filhote nasce domesticado por padrão
            babyOwl.setTame(true);

            level.addFreshEntity(babyOwl);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CHICKEN_EGG, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("HasEgg", this.hasEgg);
        tag.putInt("HatchProgress", this.hatchProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.hasEgg = tag.getBoolean("HasEgg");
        this.hatchProgress = tag.getInt("HatchProgress");
    }
}