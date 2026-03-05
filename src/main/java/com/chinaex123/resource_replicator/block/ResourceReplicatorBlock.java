package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.ResourceReplicatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public class ResourceReplicatorBlock extends BaseEntityBlock {
    private final int tier;

    public static final MapCodec<ResourceReplicatorBlock> CODEC = simpleCodec(ResourceReplicatorBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ResourceReplicatorBlock(Properties properties) {
        super(properties);
        this.tier = 1;
    }

    public ResourceReplicatorBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        ResourceReplicatorBlockEntity blockEntity = new ResourceReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof ResourceReplicatorBlockEntity) {
                ResourceReplicatorBlockEntity.serverTick(lvl, pos, st, (ResourceReplicatorBlockEntity) be);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ResourceReplicatorBlockEntity replicator) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!heldItem.isEmpty()) {
                if (replicator.addItem(heldItem)) {
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    return InteractionResult.CONSUME;
                }
            }

            if (replicator.getDisplayedItem().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.resource_replicator.empty"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.resource_replicator.contains",
                        replicator.getDisplayedItem().getHoverName()), true);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
