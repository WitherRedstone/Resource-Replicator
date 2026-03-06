package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidReplicatorBlock extends BaseEntityBlock {
    private final int tier;

    public static final MapCodec<FluidReplicatorBlock> CODEC = simpleCodec(FluidReplicatorBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public FluidReplicatorBlock(Properties properties) {
        super(properties);
        this.tier = 1;
    }

    public FluidReplicatorBlock(Properties properties, int tier) {
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
        FluidReplicatorBlockEntity blockEntity = new FluidReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof FluidReplicatorBlockEntity) {
                FluidReplicatorBlockEntity.serverTick(lvl, pos, st, (FluidReplicatorBlockEntity) be);
            }
        };
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
            // 显示当前方块中的流体
            if (replicator.getInputFluid().isEmpty() && replicator.getOutputFluid().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.resource_replicator.empty"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.fluid_replicator.contains",
                        replicator.getInputFluid().getHoverName()), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
            // 如果玩家手持空桶，清除所有流体（不需要潜行）
            if (stack.getItem() == Items.BUCKET) {
                if (!replicator.getInputFluid().isEmpty() || !replicator.getOutputFluid().isEmpty()) {
                    replicator.clearAllFluids();

                    // 播放流体倾倒声音
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, player.getSoundSource(), 1.0F, 1.0F);

                    player.displayClientMessage(Component.translatable("message.fluid_replicator.cleared"), true);
                    return ItemInteractionResult.sidedSuccess(false);
                } else {
                    player.displayClientMessage(Component.translatable("message.resource_replicator.empty"), true);
                    return ItemInteractionResult.sidedSuccess(false);
                }
            }

            // 尝试将玩家手中的流体倒入复制机
            IFluidHandler handler = replicator.getFluidHandler(null);
            if (handler != null) {
                var result = FluidUtil.tryEmptyContainer(stack, handler, Integer.MAX_VALUE, player, true);
                if (result.isSuccess()) {
                    // 成功放入流体，更新玩家手中的物品为倒出流体后的容器
                    player.setItemInHand(hand, result.getResult());

                    // 显示提示
                    player.displayClientMessage(Component.translatable("message.resource_replicator.inserted",
                            replicator.getInputFluid().getHoverName()), true);
                    return ItemInteractionResult.sidedSuccess(false);
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
