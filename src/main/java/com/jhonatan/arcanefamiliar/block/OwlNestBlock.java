package com.jhonatan.arcanefamiliar.block;

import com.jhonatan.arcanefamiliar.item.custom.OwlEggItem;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class OwlNestBlock extends BaseEntityBlock {

    public OwlNestBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwlNestBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof OwlNestBlockEntity nest) {
            ItemStack heldItem = player.getItemInHand(hand);

            // Se o ninho está vazio e o jogador clica com o Ovo de Coruja
            if (!nest.hasEgg() && heldItem.getItem() instanceof OwlEggItem) {
                if (!level.isClientSide) {
                    nest.setHasEgg(true);
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            // Se o ninho tem ovo e o jogador clica com a mão vazia (para recuperar o ovo)
            else if (nest.hasEgg() && heldItem.isEmpty()) {
                if (!level.isClientSide) {
                    nest.setHasEgg(false);
                    player.addItem(new ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.OWL_EGG.get()));
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        // Ocupa o bloco todo nas bordas (0.0 a 1.0) e tem 3 pixels de altura (0.1875D)
        return net.minecraft.world.phys.shapes.Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.OWL_NEST_BE.get(), OwlNestBlockEntity::tick);
    }
}