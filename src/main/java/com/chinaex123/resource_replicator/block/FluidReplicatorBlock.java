package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
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
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
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
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FluidReplicatorBlockEntity blockEntity = new FluidReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    /**
     * 获取方块实体的刻更新器（Ticker）
     * <p>
     * 此方法用于注册方块实体的 tick 更新逻辑。仅在服务器端注册 tick 更新器，客户端不执行任何逻辑。
     * 当方块实体需要每刻更新时（如流体复制机的工作进度），会调用注册的更新器。
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
            if (be instanceof FluidReplicatorBlockEntity) {
                FluidReplicatorBlockEntity.serverTick(lvl, pos, st, (FluidReplicatorBlockEntity) be);
            }
        };
    }

    /**
     * 处理玩家空手右键点击方块的行为
     * <p>
     * 当玩家不持有任何物品时右键点击流体复制机，会显示当前机器中存储的流体信息。
     * 如果输入罐和输出罐都为空，会提示"空"；否则显示输入罐中流体的名称。
     * </p>
     * 
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return InteractionResult 交互结果，客户端直接返回 SUCCESS，服务端根据操作结果返回相应值
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
            // 显示当前方块中的流体
            if (replicator.getInputFluid().isEmpty() && replicator.getOutputFluid().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.fluid_replicator.empty"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.fluid_replicator.contains",
                        replicator.getInputFluid().getHoverName()).withStyle(style -> style.withColor(ChatFormatting.AQUA)), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * 处理玩家手持物品右键点击方块的行为
     * <p>
     * 此方法处理玩家手持流体容器与复制机的交互，主要功能包括：
     * </p>
     * <ul>
     *     <li><strong>清空机器</strong>：当玩家手持空桶右键时，清除机器内的所有流体（无需潜行）</li>
     *     <li><strong>注入流体</strong>：将玩家容器中的流体倒入机器（使用 FluidUtil 自动处理容器）</li>
     *     <li><strong>创造模式保护</strong>：在创造模式下不消耗流体桶</li>
     *     <li><strong>数据同步</strong>：成功操作后更新玩家手中的物品并显示提示信息</li>
     * </ul>
     * <p>
     * 该方法使用 NeoForge 的 FluidUtil 工具类来处理流体容器的倒出逻辑，支持各种流体桶和容器。
     * 成功时会播放倒流体的声音并显示提示信息。
     * </p>
     * 
     * @param stack 玩家手持的物品堆
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hand 使用的交互手（主手或副手）
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return ItemInteractionResult 物品交互结果，成功时返回 sidedSuccess，失败时返回 PASS_TO_DEFAULT_BLOCK_INTERACTION
     */
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

                    player.displayClientMessage(Component.translatable("message.fluid_replicator.cleared")
                            .withStyle(style -> style.withColor(ChatFormatting.YELLOW)), true);
                    return ItemInteractionResult.sidedSuccess(false);
                } else {
                    player.displayClientMessage(Component.translatable("message.fluid_replicator.empty"), true);
                    return ItemInteractionResult.sidedSuccess(false);
                }
            }

            // 尝试将玩家手中的流体倒入复制机
            IFluidHandler handler = replicator.getFluidHandler(null);
            if (handler != null) {
                var result = FluidUtil.tryEmptyContainer(stack, handler, Integer.MAX_VALUE, player, true);
                if (result.isSuccess()) {
                    // 成功放入流体，更新玩家手中的物品为倒出流体后的容器
                    // 创造模式不消耗流体桶
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, result.getResult());
                    }

                    // 显示提示
                    player.displayClientMessage(Component.translatable("message.fluid_replicator.inserted",
                            replicator.getInputFluid().getHoverName()).withStyle(style -> style.withColor(ChatFormatting.GOLD)), true);

                    return ItemInteractionResult.sidedSuccess(false);
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
