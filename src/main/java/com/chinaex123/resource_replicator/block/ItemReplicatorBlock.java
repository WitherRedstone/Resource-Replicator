package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ItemReplicatorBlock extends BaseEntityBlock {
    private final int tier;

    public static final MapCodec<ItemReplicatorBlock> CODEC = simpleCodec(ItemReplicatorBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ItemReplicatorBlock(Properties properties) {
        super(properties);
        this.tier = 1;
    }

    public ItemReplicatorBlock(Properties properties, int tier) {
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
        ItemReplicatorBlockEntity blockEntity = new ItemReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof ItemReplicatorBlockEntity) {
                ItemReplicatorBlockEntity.serverTick(lvl, pos, st, (ItemReplicatorBlockEntity) be);
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ItemReplicatorBlockEntity replicator) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            // 如果玩家潜行（Shift），尝试取出物品
            if (player.isShiftKeyDown()) {
                ItemStack extractedItem = replicator.extractItem();
                if (!extractedItem.isEmpty()) {
                    // 如果玩家手持物品为空，直接给予取出的物品
                    if (heldItem.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, extractedItem);
                    } else {
                        // 否则将物品掉落到地上
                        player.drop(extractedItem, false);
                    }
                    player.displayClientMessage(Component.translatable("message.resource_replicator.extracted",
                            extractedItem.getHoverName()), true);
                    return InteractionResult.CONSUME;
                } else {
                    player.displayClientMessage(Component.translatable("message.resource_replicator.empty"), true);
                    return InteractionResult.CONSUME;
                }
            }

            // 正常模式：放入物品
            if (!heldItem.isEmpty()) {
                if (replicator.addItem(heldItem)) {
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    player.displayClientMessage(Component.translatable("message.resource_replicator.inserted",
                            heldItem.getHoverName()), true);
                    return InteractionResult.CONSUME;
                }
            }

            // 显示当前方块中的物品
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
