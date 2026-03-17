package com.chinaex123.resource_replicator.block;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流体复制机方块类
 * 继承自 BaseEntityBlock，支持方块实体功能
 */
public class FluidReplicatorBlock extends BaseEntityBlock {
    // 创建日志记录器实例
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidReplicatorBlock.class);

    // 机器等级（默认为 1 级）
    private final int tier;

    // 定义方块的编解码器
    public static final MapCodec<FluidReplicatorBlock> CODEC = simpleCodec(FluidReplicatorBlock::new);

    /**
     * 获取方块的编解码器
     */
    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * 构造函数（默认 1 级）
     */
    public FluidReplicatorBlock(Properties properties) {
        super(properties);
        this.tier = 1;
    }

    /**
     * 构造函数（指定等级）
     */
    public FluidReplicatorBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    /**
     * 获取方块的渲染形状
     */
    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * 创建新的方块实体
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FluidReplicatorBlockEntity blockEntity = new FluidReplicatorBlockEntity(pos, state);
        blockEntity.setTier(tier);
        return blockEntity;
    }

    /**
     * 获取方块实体的刻更新器（Ticker）
     *
     * @param level 当前世界实例，用于判断是客户端还是服务端
     * @param state 方块的当前状态
     * @param type 方块实体类型，用于匹配正确的方块实体
     * @return BlockEntityTicker<T> 方块实体的刻更新器，如果是客户端则返回 null，否则返回执行 serverTick() 的更新器
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, pos, st, be) -> {
            if (be instanceof FluidReplicatorBlockEntity) {
                FluidReplicatorBlockEntity.serverTick(lvl, pos, st, (FluidReplicatorBlockEntity) be);
            }
        };
    }

    /**
     * 处理玩家右键点击方块的行为（未手持物品时）
     */
    protected @NotNull InteractionResult useWithoutItem(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
            if (replicator.getInputFluid().isEmpty() && replicator.getOutputFluid().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.fluid_replicator.empty"), true);
            } else {
                FluidStack fluidStack = replicator.getInputFluid();
                Component fluidName = fluidStack.getFluid().getFluidType().getDescription();
                player.displayClientMessage(Component.translatable("message.fluid_replicator.contains",
                        fluidName).withStyle(style -> style.withColor(ChatFormatting.AQUA)), true);
            }
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * 处理玩家手持物品右键点击方块的行为
     *
     * @param stack 玩家手持的物品堆
     * @param state 方块的当前状态
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param player 执行操作的玩家
     * @param hand 使用的交互手（主手或副手）
     * @param hitResult 点击检测结果，包含点击位置和面等信息
     * @return InteractionResult 物品交互结果，成功时返回 CONSUME，失败时返回 PASS
     */
    @Override
    protected @NotNull InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                   @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
            if (stack.getItem() == Items.BUCKET) {
                if (!replicator.getInputFluid().isEmpty() || !replicator.getOutputFluid().isEmpty()) {
                    replicator.clearAllFluids();

                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, player.getSoundSource(), 1.0F, 1.0F);

                    player.displayClientMessage(Component.translatable("message.fluid_replicator.cleared")
                            .withStyle(style -> style.withColor(ChatFormatting.YELLOW)), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.fluid_replicator.empty"), true);
                }
                return InteractionResult.CONSUME;
            }

            var fluidHandler = replicator.getFluidHandler(null);
            if (fluidHandler != null) {
                FluidStack fluidStack = FluidUtil.getFirstStackContained(stack);

                if (fluidStack.isEmpty() && stack.getItem() == Items.MILK_BUCKET) {
                    var milkOpt = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(net.minecraft.resources.Identifier.withDefaultNamespace("milk"));
                    if (milkOpt.isPresent()) {
                        net.minecraft.world.level.material.Fluid milkFluid = milkOpt.get().value();
                        if (!milkFluid.equals(net.minecraft.world.level.material.Fluids.EMPTY)) {
                            fluidStack = new FluidStack(milkFluid, 1000);
                        }
                    }
                }

                if (!fluidStack.isEmpty()) {
                    FluidResource resource = FluidResource.of(fluidStack.getFluid());
                    int actualAmount = fluidStack.getAmount();

                    try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                        FluidReplicatorBlockEntity.setPlayerOperation(true);

                        var inserted = fluidHandler.insert(0, resource, actualAmount, transaction);

                        FluidReplicatorBlockEntity.setPlayerOperation(false);

                        if (inserted > 0) {
                            transaction.commit();

                            var itemAccess = net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack).oneByOne();
                            var itemFluidHandler = itemAccess.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Fluid.ITEM);

                            if (itemFluidHandler != null) {
                                try (var itemTransaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                                    itemFluidHandler.extract(0, resource, inserted, itemTransaction);
                                    itemTransaction.commit();
                                }
                            } else {
                                if (!player.isCreative()) {
                                    stack.shrink(1);
                                    ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                                    if (!player.getInventory().add(emptyBucket)) {
                                        player.drop(emptyBucket, false);
                                    }
                                }
                            }

                            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, player.getSoundSource(), 1.0F, 1.0F);

                            replicator.markUpdated();

                            player.displayClientMessage(Component.translatable("message.fluid_replicator.inserted",
                                            fluidStack.getFluid().getFluidType().getDescription())
                                    .withStyle(style -> style.withColor(ChatFormatting.GOLD)), true);
                            return InteractionResult.CONSUME;
                        }
                    }
                }
            }

            if (replicator.getInputFluid().isEmpty() && replicator.getOutputFluid().isEmpty()) {
                player.displayClientMessage(Component.translatable("message.fluid_replicator.empty"), true);
            } else {
                FluidStack fluidStack = replicator.getInputFluid();
                if (!fluidStack.isEmpty()) {
                    Component fluidName = fluidStack.getFluid().getFluidType().getDescription();
                    player.displayClientMessage(Component.translatable("message.fluid_replicator.contains",
                            fluidName).withStyle(style -> style.withColor(ChatFormatting.AQUA)), true);
                }
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
