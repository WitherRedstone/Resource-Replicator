package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.config.RRServerConfig;
import com.chinaex123.resource_replicator.init.RRBlockEntities;
import com.chinaex123.resource_replicator.network.FluidSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 流体复制机方块实体。
 * <p>
 * 负责处理输入流体的存储、按 tier 配置进行流体复制、
 * 输出到相邻容器或自身输出罐、能量消耗以及客户端同步。
 */
public class FluidReplicatorBlockEntity extends BlockEntity {

    /**
     * 输入罐固定容量（mB）。
     */
    private static final int INPUT_TANK_CAPACITY = 1000;

    /**
     * 线程局部标记：当前操作是否由玩家触发。
     */
    private static final ThreadLocal<Boolean> IS_PLAYER_OPERATION = ThreadLocal.withInitial(() -> false);

    /**
     * 线程局部标记：当前操作是否为内部复制。
     */
    private static final ThreadLocal<Boolean> IS_INTERNAL_REPLICATION = ThreadLocal.withInitial(() -> false);

    /**
     * 输出罐容量，由 tier 配置决定。
     */
    private long outputCapacity;

    /**
     * 设置玩家操作标记。
     *
     * @param isPlayer 是否为玩家操作
     */
    public static void setPlayerOperation(boolean isPlayer) {
        IS_PLAYER_OPERATION.set(isPlayer);
    }

    /**
     * 获取玩家操作标记。
     *
     * @return 是否为玩家操作
     */
    public static boolean isPlayerOperation() {
        return IS_PLAYER_OPERATION.get();
    }

    /**
     * 设置内部复制标记。
     *
     * @param isInternal 是否为内部复制
     */
    public static void setInternalReplication(boolean isInternal) {
        IS_INTERNAL_REPLICATION.set(isInternal);
    }

    /**
     * 获取内部复制标记。
     *
     * @return 是否为内部复制
     */
    public static boolean isInternalReplication() {
        return IS_INTERNAL_REPLICATION.get();
    }

    /**
     * tick 计数器，用于控制处理速度。
     */
    private int tickCounter = 0;

    /**
     * 当前 tier。
     */
    public FluidReplicatorTier tier = FluidReplicatorTier.FLUID_TIER_1;

    /**
     * 当前存储的能量。
     */
    private long energyStored;

    /**
     * 能量容量，由 tier 配置决定。
     */
    private long energyCapacity;

    /**
     * 每 1000mB 流体复制消耗的能量，由 tier 配置决定。
     */
    private long energyConsumption;

    {
        updateEnergyStats();
        updateOutputCapacity();
        energyStored = 0;
    }

    /**
     * 根据当前 tier 更新能量容量与能耗配置。
     */
    private void updateEnergyStats() {
        switch (tier) {
            case FLUID_TIER_1:
                energyCapacity = RRServerConfig.FLUID_TIER1_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.FLUID_TIER1_ENERGY_CONSUMPTION.get();
                break;
            case FLUID_TIER_2:
                energyCapacity = RRServerConfig.FLUID_TIER2_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.FLUID_TIER2_ENERGY_CONSUMPTION.get();
                break;
            case FLUID_TIER_3:
                energyCapacity = RRServerConfig.FLUID_TIER3_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.FLUID_TIER3_ENERGY_CONSUMPTION.get();
                break;
            case FLUID_TIER_4:
                energyCapacity = RRServerConfig.FLUID_TIER4_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.FLUID_TIER4_ENERGY_CONSUMPTION.get();
                break;
            case FLUID_TIER_5:
                energyCapacity = RRServerConfig.FLUID_TIER5_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.FLUID_TIER5_ENERGY_CONSUMPTION.get();
                break;
        }
    }

    /**
     * 根据当前 tier 更新输出罐容量。
     */
    private void updateOutputCapacity() {
        this.outputCapacity = switch (tier) {
            case FLUID_TIER_1 -> RRServerConfig.FLUID_TIER1_OUTPUT_TANK_CAPACITY.get();
            case FLUID_TIER_2 -> RRServerConfig.FLUID_TIER2_OUTPUT_TANK_CAPACITY.get();
            case FLUID_TIER_3 -> RRServerConfig.FLUID_TIER3_OUTPUT_TANK_CAPACITY.get();
            case FLUID_TIER_4 -> RRServerConfig.FLUID_TIER4_OUTPUT_TANK_CAPACITY.get();
            case FLUID_TIER_5 -> RRServerConfig.FLUID_TIER5_OUTPUT_TANK_CAPACITY.get();
        };
    }

    /**
     * 设置 tier 并刷新能量与输出容量配置。
     *
     * @param tierId tier 编号
     */
    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputCapacity();
    }

    /**
     * 能量处理器，仅允许输入，不允许抽出。
     */
    private final EnergyHandler energyHandler = new EnergyHandler() {
        /**
         * 向内部能量存储插入能量。
         *
         * @param maxReceive 最大可接收量
         * @param transaction 事务上下文
         * @return 实际接收量
         */
        @Override
        public int insert(int maxReceive, @NotNull TransactionContext transaction) {
            // 计算可以接收的能量
            int canReceive = (int) Math.min(maxReceive, energyCapacity - energyStored);
            if (canReceive <= 0) {
                return 0;
            }
            energyStored += canReceive;
            markUpdated();
            return canReceive;
        }

        /**
         * 从内部能量存储抽出能量，本机不允许抽出。
         *
         * @param maxExtract 最大可抽出量
         * @param transaction 事务上下文
         * @return 始终为 0
         */
        @Override
        public int extract(int maxExtract, @NotNull TransactionContext transaction) {
            return 0;
        }

        /**
         * 获取当前存储的能量。
         *
         * @return 当前能量
         */
        @Override
        public long getAmountAsLong() {
            return energyStored;
        }

        /**
         * 获取能量容量。
         *
         * @return 能量容量
         */
        @Override
        public long getCapacityAsLong() {
            return energyCapacity;
        }
    };

    /**
     * 可清空的流体处理器。
     * <p>
     * 槽位 0 为输入罐，槽位 1 为输出罐。
     * 输入罐仅允许玩家操作或配置允许时插入；
     * 输出罐仅允许内部复制插入，且默认不允许抽出。
     */
    private class ClearableFluidHandler extends FluidStacksResourceHandler {
        /**
         * 是否允许抽出（仅用于 clearAll 临时开启）。
         */
        private boolean allowClearing = false;

        /**
         * 输入罐容量。
         */
        private final int inputTankCapacity;

        /**
         * 构造可清空流体处理器。
         *
         * @param size             槽位数量
         * @param capacity         默认容量
         * @param inputTankCapacity 输入罐容量
         */
        public ClearableFluidHandler(int size, int capacity, int inputTankCapacity) {
            super(size, capacity);
            this.inputTankCapacity = inputTankCapacity;
        }

        /**
         * 向指定槽位插入流体。
         * <p>
         * 槽位 1（输出罐）仅允许内部复制插入；
         * 槽位 0（输入罐）仅允许玩家操作插入，
         * 或在配置允许销毁时接受管道插入。
         *
         * @param slot       槽位
         * @param resource   流体资源
         * @param maxAmount  最大插入量
         * @param transaction 事务上下文
         * @return 实际插入量
         */
        @Override
        public int insert(int slot, FluidResource resource, int maxAmount, @NotNull TransactionContext transaction) {
            // 槽位 1：输出罐
            if (slot == 1) {
                // 输出罐只允许内部复制操作插入
                if (!isInternalReplication()) {
                    return 0;
                }

                FluidResource currentResource = getResource(1);
                long currentAmount = getAmountAsLong(1);

                // 空罐子，直接放入
                if (currentResource.isEmpty() || currentAmount == 0) {
                    set(1, resource, maxAmount);
                    markUpdated();
                    return maxAmount;
                } else if (currentResource.equals(resource)) {
                    int canAdd = Math.min((int) outputCapacity - (int) currentAmount, maxAmount);
                    if (canAdd > 0) {
                        set(1, resource, (int) (currentAmount + canAdd));
                        markUpdated();
                    }
                    return canAdd;
                }
                return 0;
            }

            // 只能使用槽位 0
            if (slot != 0) {
                return 0;
            }

            // 判断是否是管道插入
            boolean isPipeInsertion = !isPlayerOperation();

            // 管道插入且开启销毁模式
            if (isPipeInsertion && RRServerConfig.FLUID_REPLICATOR_ENABLE_DESTROY.get()) {
                return maxAmount;
            }

            // 管道插入但关闭销毁模式：拒绝
            if (isPipeInsertion) {
                return 0;
            }

            // 玩家操作：正常插入逻辑
            FluidResource currentResource = getResource(0);
            long currentAmount = getAmountAsLong(0);

            if (currentResource.isEmpty() || currentAmount == 0) {
                set(0, resource, maxAmount);
                markUpdated();
                return maxAmount;
            } else if (currentResource.equals(resource)) {
                int canAdd = Math.min(inputTankCapacity - (int) currentAmount, maxAmount);
                if (canAdd > 0) {
                    set(0, resource, (int) (currentAmount + canAdd));
                    markUpdated();
                }
                return canAdd;
            }

            return 0;
        }

        /**
         * 从指定槽位抽出流体。
         * <p>
         * 默认仅允许从输出罐（槽位 1）抽出，
         * 清空操作期间临时允许所有槽位抽出。
         *
         * @param slot       槽位
         * @param resource   流体资源
         * @param amount     抽出量
         * @param transaction 事务上下文
         * @return 实际抽出量
         */
        @Override
        public int extract(int slot, FluidResource resource, int amount, @NotNull TransactionContext transaction) {
            // 非清空模式下，只允许从输出罐抽取
            if (!allowClearing && slot != 1) {
                return 0;
            }
            return super.extract(slot, resource, amount, transaction);
        }

        /**
         * 清空所有流体罐。
         */
        public void clearAll() {
            allowClearing = true;
            try {
                // 清空输入罐
                FluidResource inputResource = getResource(0);
                long inputAmount = getAmountAsLong(0);
                if (!inputResource.isEmpty() && inputAmount > 0) {
                    set(0, FluidResource.EMPTY, 0);
                }

                // 清空输出罐
                FluidResource outputResource = getResource(1);
                long outputAmount = getAmountAsLong(1);
                if (!outputResource.isEmpty() && outputAmount > 0) {
                    set(1, FluidResource.EMPTY, 0);
                }
            } finally {
                allowClearing = false;
            }
        }

        /**
         * 获取指定槽位的容量。
         *
         * @param index    槽位
         * @param resource 流体资源
         * @return 该槽位容量
         */
        @Override
        protected int getCapacity(int index, @NotNull FluidResource resource) {
            if (index == 0) return inputTankCapacity;
            if (index == 1) return (int) outputCapacity;
            return super.getCapacity(index, resource);
        }
    }

    /**
     * 流体处理器实例。
     */
    private final ClearableFluidHandler fluidHandler =
            new ClearableFluidHandler(2, (int) outputCapacity, INPUT_TANK_CAPACITY);

    /**
     * 清空所有流体并标记更新。
     */
    public void clearAllFluids() {
        fluidHandler.clearAll();
        markUpdated();
    }

    /**
     * 服务端 tick 逻辑。
     * <p>
     * 根据处理速度、能量与输出配置执行流体复制，
     * 优先自动输出到相邻容器，剩余部分写入输出罐。
     *
     * @param level       世界
     * @param pos         方块位置
     * @param state       方块状态
     * @param blockEntity 方块实体
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        boolean hasFluidChanged = false;

        blockEntity.tickCounter++;

        // 获取输入罐的流体
        FluidResource inputResource = blockEntity.fluidHandler.getResource(0);
        long inputAmount = blockEntity.fluidHandler.getAmountAsLong(0);

        // 没有输入流体，跳过
        if (inputResource.isEmpty() || inputAmount == 0) {
            if (blockEntity.fluidHandler.getAmountAsLong(1) > 0) {
                blockEntity.clearFluidHandlerDirectly();
                hasFluidChanged = true;
            }
            if (hasFluidChanged) {
                blockEntity.markUpdated();
            }
            return;
        }

        // 未达到生产时间，跳过
        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            if (hasFluidChanged) {
                blockEntity.markUpdated();
            }
            return;
        }

        blockEntity.tickCounter = 0;

        // 计算实际输出量
        int actualOutput = blockEntity.tier.getActualOutputAmount(inputResource.toStack((int) inputAmount));

        long energyPer1000MB = blockEntity.energyConsumption;
        long energyNeeded = (actualOutput * energyPer1000MB) / 1000;
        if (energyNeeded < 1 && actualOutput > 0) {
            energyNeeded = 1;
        }

        // 能量不足，跳过
        if (blockEntity.energyStored < energyNeeded) {
            if (hasFluidChanged) {
                blockEntity.markUpdated();
            }
            return;
        }

        int remainingOutput = actualOutput;
        boolean hasUpdated = false;
        int totalOutput = 0;

        boolean autoOutputEnabled = RRServerConfig.FLUID_REPLICATOR_AUTO_OUTPUT.get();

        // 自动输出到相邻容器
        if (autoOutputEnabled) {
            Direction outputDirection = RRServerConfig.FLUID_REPLICATOR_AUTO_OUTPUT_DIRECTION.get();
            BlockPos neighborPos = pos.relative(outputDirection);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!(neighborState.getBlock() instanceof FluidReplicatorBlock)) {
                var fluidCapability = Capabilities.Fluid.BLOCK;
                ResourceHandler<@NotNull FluidResource> handler = level.getCapability(
                        fluidCapability,
                        neighborPos,
                        neighborState,
                        level.getBlockEntity(neighborPos),
                        outputDirection.getOpposite()
                );

                if (handler != null) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        long filled = handler.insert(inputResource, remainingOutput, transaction);

                        if (filled > 0) {
                            transaction.commit();
                            totalOutput += (int) filled;
                            remainingOutput -= (int) filled;

                            // 从输入罐提取相应数量的流体
                            blockEntity.fluidHandler.extract(0, inputResource, (int) filled, transaction);

                            hasUpdated = true;
                            hasFluidChanged = true;
                        }
                    }
                }
            }
        }

        // 将剩余流体放入输出罐
        if (remainingOutput > 0) {
            setInternalReplication(true);
            try (Transaction transaction = Transaction.openRoot()) {
                int inserted = blockEntity.fluidHandler.insert(1, inputResource, remainingOutput, transaction);
                if (inserted > 0) {
                    transaction.commit();
                    totalOutput += inserted;
                    hasUpdated = true;
                    hasFluidChanged = true;
                }
            } finally {
                setInternalReplication(false);
            }
        }

        // 消耗能量并标记更新
        if (hasUpdated && totalOutput > 0) {
            long actualEnergyNeeded = (totalOutput * energyPer1000MB) / 1000;
            if (actualEnergyNeeded < 1) {
                actualEnergyNeeded = 1;
            }

            if (blockEntity.energyStored >= actualEnergyNeeded) {
                blockEntity.energyStored -= actualEnergyNeeded;
            }
        }

        if (hasFluidChanged) {
            blockEntity.markUpdated();
        }
    }

    /**
     * 设置所在世界。
     * <p>
     * 客户端世界设置后立即标记更新，确保渲染数据同步。
     *
     * @param level 世界
     */
    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            markUpdated();
        }
    }

    /**
     * 构造流体复制机方块实体。
     *
     * @param pos   方块位置
     * @param state 方块状态
     */
    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.FLUID_REPLICATOR.get(), pos, state);
    }

    /**
     * 保存附加数据到 NBT。
     * <p>
     * 包含 tickCounter、tier、能量以及输入/输出罐的流体信息。
     *
     * @param output 数据输出
     */
    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);

        CompoundTag customData = new CompoundTag();
        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putLong("energyStored", energyStored);

        CompoundTag tanksTag = new CompoundTag();

        // 保存输入罐
        FluidResource inputResource = fluidHandler.getResource(0);
        long inputAmount = fluidHandler.getAmountAsLong(0);

        if (!inputResource.isEmpty() && inputAmount > 0) {
            CompoundTag inputTag = new CompoundTag();
            inputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(inputResource.toStack(1).getFluid()).toString());
            inputTag.putInt("amount", (int) inputAmount);
            tanksTag.put("inputTank", inputTag);
        }

        // 保存输出罐
        FluidResource outputResource = fluidHandler.getResource(1);
        long outputAmount = fluidHandler.getAmountAsLong(1);

        if (!outputResource.isEmpty() && outputAmount > 0) {
            CompoundTag outputTag = new CompoundTag();
            outputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(outputResource.toStack(1).getFluid()).toString());
            outputTag.putInt("amount", (int) outputAmount);
            tanksTag.put("outputTank", outputTag);
        }

        customData.put("tanks", tanksTag);

        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    /**
     * 获取更新标签，用于客户端同步。
     * <p>
     * 包含 tickCounter、tier、能量以及输入/输出罐的流体信息。
     *
     * @param registries 注册表提供者
     * @return 更新用 CompoundTag
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag outerTag = new CompoundTag();
        CompoundTag customData = new CompoundTag();

        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putLong("energyStored", energyStored);

        CompoundTag tanksTag = new CompoundTag();

        FluidResource inputRes = fluidHandler.getResource(0);
        long inputAmt = fluidHandler.getAmountAsLong(0);

        if (!inputRes.isEmpty() && inputAmt > 0) {
            CompoundTag inputTag = new CompoundTag();
            FluidStack stack = inputRes.toStack((int) inputAmt);
            inputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            inputTag.putInt("amount", stack.getAmount());
            tanksTag.put("inputTank", inputTag);
        }

        FluidResource outputRes = fluidHandler.getResource(1);
        long outputAmt = fluidHandler.getAmountAsLong(1);

        if (!outputRes.isEmpty() && outputAmt > 0) {
            CompoundTag outputTag = new CompoundTag();
            FluidStack stack = outputRes.toStack((int) outputAmt);
            outputTag.putString("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
            outputTag.putInt("amount", stack.getAmount());
            tanksTag.put("outputTank", outputTag);
        }

        customData.put("tanks", tanksTag);
        outerTag.put("custom_data", customData);

        return outerTag;
    }

    /**
     * 从 NBT 加载附加数据。
     * <p>
     * 恢复 tickCounter、tier、能量以及输入/输出罐的流体信息，
     * 并刷新能量与输出容量配置。
     *
     * @param input 数据输入
     */
    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);

        CompoundTag customData = input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());

        tickCounter = customData.getInt("tickCounter").orElse(0);
        if (customData.contains("tier")) {
            this.tier = FluidReplicatorTier.fromId(customData.getInt("tier").orElse(0));
        }
        if (customData.contains("energyStored")) {
            this.energyStored = customData.getLong("energyStored").orElse(0L);
        }

        CompoundTag tanksTag = customData.getCompound("tanks").orElse(new CompoundTag());

        // 加载输入罐
        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            String fluidName = inputTag.getString("fluid").orElse("");
            int amount = inputTag.getInt("amount").orElse(0);

            if (!fluidName.isEmpty() && amount > 0) {
                Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(fluidName));
                if (!fluid.equals(Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(0, resource, amount);
                }
            }
        }

        // 加载输出罐
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            String fluidName = outputTag.getString("fluid").orElse("");
            int amount = outputTag.getInt("amount").orElse(0);

            if (!fluidName.isEmpty() && amount > 0) {
                Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(fluidName));
                if (!fluid.equals(Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(1, resource, amount);
                }
            }
        }

        updateEnergyStats();
        updateOutputCapacity();

        setChanged();
    }

    /**
     * 处理客户端更新标签。
     * <p>
     * 清空本地流体处理器后，从数据中恢复 tickCounter、tier、
     * 能量以及输入/输出罐的流体信息。
     *
     * @param input 数据输入
     */
    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        CompoundTag customData = input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());

        tickCounter = customData.getInt("tickCounter").orElse(0);
        if (customData.contains("tier")) {
            this.tier = FluidReplicatorTier.fromId(customData.getInt("tier").orElse(0));
        }
        if (customData.contains("energyStored")) {
            this.energyStored = customData.getLong("energyStored").orElse(0L);
        }

        CompoundTag tanksTag = customData.getCompound("tanks").orElse(new CompoundTag());

        clearFluidHandlerDirectly();

        // 加载输入罐
        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            String fluidName = inputTag.getString("fluid").orElse("");
            int amount = inputTag.getInt("amount").orElse(0);

            if (!fluidName.isEmpty() && amount > 0) {
                Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(fluidName));
                if (!fluid.equals(Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(0, resource, amount);
                }
            }
        }

        // 加载输出罐
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            String fluidName = outputTag.getString("fluid").orElse("");
            int amount = outputTag.getInt("amount").orElse(0);

            if (!fluidName.isEmpty() && amount > 0) {
                Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.tryParse(fluidName));
                if (!fluid.equals(Fluids.EMPTY)) {
                    FluidResource resource = FluidResource.of(fluid);
                    fluidHandler.set(1, resource, amount);
                }
            }
        }
    }

    /**
     * 从网络数据包加载流体内容。
     * <p>
     * 仅在客户端执行，用于同步输入/输出罐的流体类型与数量。
     *
     * @param tag 包含流体数据的 CompoundTag
     */
    public void loadFluidFromPacket(CompoundTag tag) {
        if (getLevel() != null && !getLevel().isClientSide()) {
            return;
        }

        CompoundTag data = tag.contains("custom_data")
                ? tag.getCompound("custom_data").orElse(tag)
                : tag;

        clearFluidHandlerDirectly();

        CompoundTag tanksTag = data.getCompound("tanks").orElse(new CompoundTag());

        clearFluidHandlerDirectly();

        if (tanksTag.contains("inputTank")) {
            CompoundTag inputTag = tanksTag.getCompound("inputTank").orElse(new CompoundTag());
            var fluid = BuiltInRegistries.FLUID.get(
                    Objects.requireNonNull(Identifier.tryParse(inputTag.getString("fluid").orElse("")))
            );
            int amount = inputTag.getInt("amount").orElse(0);

            if (fluid.isPresent() && amount > 0) {
                var resource = FluidResource.of(fluid.get());
                fluidHandler.set(0, resource, amount);
            }
        }

        // 加载输出罐
        if (tanksTag.contains("outputTank")) {
            CompoundTag outputTag = tanksTag.getCompound("outputTank").orElse(new CompoundTag());
            var fluid = BuiltInRegistries.FLUID.get(
                    Objects.requireNonNull(Identifier.tryParse(outputTag.getString("fluid").orElse("")))
            );
            int amount = outputTag.getInt("amount").orElse(0);

            if (fluid.isPresent() && amount > 0) {
                var resource = FluidResource.of(fluid.get());
                fluidHandler.set(1, resource, amount);
            }
        }
    }

    /**
     * 直接清空流体处理器中的所有流体，不触发额外逻辑。
     */
    private void clearFluidHandlerDirectly() {
        fluidHandler.set(0, FluidResource.EMPTY, 0);
        fluidHandler.set(1, FluidResource.EMPTY, 0);
    }

    /**
     * 获取方块实体的更新数据包，用于客户端同步。
     *
     * @return 更新数据包
     */
    @Nullable
    @Override
    public Packet<@NotNull ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * 标记方块实体已更新。
     * <p>
     * 调用 setChanged，向周围客户端发送方块更新，
     * 并对 64 格范围内的玩家发送流体同步数据包。
     */
    public void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            if (!level.isClientSide()) {
                var tag = getUpdateTag(level.registryAccess());
                var packet = new FluidSyncPacket(getBlockPos(), tag);

                for (var player : level.players()) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) {
                            PacketDistributor.sendToPlayer(serverPlayer, packet);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取输入罐流体。
     *
     * @return 输入罐的 FluidStack，空则为 FluidStack.EMPTY
     */
    public FluidStack getInputFluid() {
        FluidResource resource = fluidHandler.getResource(0);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        long amount = fluidHandler.getAmountAsLong(0);

        if (amount <= 0) {
            return FluidStack.EMPTY;
        }

        return resource.toStack((int) amount);
    }

    /**
     * 获取输出罐流体。
     *
     * @return 输出罐的 FluidStack，空则为 FluidStack.EMPTY
     */
    public FluidStack getOutputFluid() {
        FluidResource resource = fluidHandler.getResource(1);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        long amount = fluidHandler.getAmountAsLong(1);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }

        return resource.toStack((int) amount);
    }

    /**
     * 获取流体处理器。
     *
     * @param side 方向，当前忽略
     * @return 流体处理器
     */
    @Nullable
    public FluidStacksResourceHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    /**
     * 获取能量处理器。
     *
     * @param side 方向，当前忽略
     * @return 能量处理器
     */
    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    /**
     * 获取当前存储的能量。
     *
     * @return 当前能量
     */
    public long getEnergyStored() {
        return energyStored;
    }

    /**
     * 获取最大能量容量。
     *
     * @return 能量容量
     */
    public long getMaxEnergyStored() {
        return energyCapacity;
    }
}