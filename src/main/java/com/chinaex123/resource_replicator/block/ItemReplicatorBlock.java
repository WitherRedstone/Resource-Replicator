package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemReplicatorBlock extends BaseEntityBlock {
    private final int tier;

    public static final MapCodec<ItemReplicatorBlock> CODEC = simpleCodec(ItemReplicatorBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
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
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        ItemReplicatorBlockEntity blockEntity = new ItemReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    /**
     * 获取方块实体的刻更新器（Ticker）
     * <p>
     * 此方法用于注册方块实体的 tick 更新逻辑。仅在服务器端注册 tick 更新器，客户端不执行任何逻辑。
     * 当方块实体需要每刻更新时（如物品复制机的工作进度），会调用注册的更新器。
     * </p>
     * 
     * @param level 当前世界实例，用于判断是客户端还是服务端
     * @param state 方块的当前状态
     * @param type 方块实体类型，用于匹配正确的方块实体
     * @return BlockEntityTicker<T> 方块实体的刻更新器，如果是客户端则返回 null，否则返回执行 serverTick() 的更新器
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof ItemReplicatorBlockEntity) {
                ItemReplicatorBlockEntity.serverTick(lvl, pos, st, (ItemReplicatorBlockEntity) be);
            }
        };
    }

    /**
     * 处理玩家空手右键点击方块的行为
     * <p>
     * 此方法处理玩家与物品复制机的交互逻辑，支持两种操作模式：
     * </p>
     * <ul>
     *     <li><strong>潜行取出</strong>：玩家按住 Shift 键右键时，从复制机输入槽取出物品。如果手持物品为空则直接拿取，否则掉落到地上</li>
     *     <li><strong>正常放入</strong>：玩家手持物品右键时，将物品放入复制机输入槽（非创造模式会消耗物品）</li>
     *     <li><strong>信息显示</strong>：如果未进行任何操作，显示当前复制机中的物品信息</li>
     * </ul>
     * <p>
     * 所有操作都会通过黑白名单过滤检查，并在操作失败时显示具体原因。成功时会播放相应音效并显示提示信息。
     * </p>
     * 
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return InteractionResult 交互结果，客户端返回 SUCCESS，服务端根据操作结果返回 CONSUME 或 SUCCESS
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, @NotNull Player player, BlockHitResult hitResult) {
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
                    player.displayClientMessage(Component.translatable("message.item_replicator.extracted",
                            extractedItem.getHoverName()).withStyle(style -> style.withColor(ChatFormatting.YELLOW)), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.item_replicator.empty"), true);
                }
                return InteractionResult.CONSUME;
            }

            // 正常模式：放入物品
            if (!heldItem.isEmpty()) {
                ReplicatorFilter.FilterResult filterResult = ReplicatorFilter.canInsertItemWithReason(heldItem);
                if (!filterResult.canInsert()) {
                    player.displayClientMessage(filterResult.getReason(), true);
                    return InteractionResult.CONSUME;
                }

                if (replicator.addItem(heldItem)) {
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    player.displayClientMessage(Component.translatable("message.item_replicator.inserted",
                            heldItem.getHoverName()).withStyle(style -> style.withColor(ChatFormatting.GOLD)), true);
                    return InteractionResult.CONSUME;
                }
            }

            // 显示当前方块中的物品
            if (replicator.getDisplayedItem().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.item_replicator.empty"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.item_replicator.contains",
                        replicator.getDisplayedItem().getHoverName()).withStyle(style -> style.withColor(ChatFormatting.AQUA)), true);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
