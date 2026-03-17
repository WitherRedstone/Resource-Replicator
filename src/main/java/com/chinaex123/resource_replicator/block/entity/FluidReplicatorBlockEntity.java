package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 流体复制机方块实体类
 * 负责处理流体复制机的所有逻辑，包括流体存储、能量管理、自动生产和网络同步
 */
public class FluidReplicatorBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidReplicatorBlockEntity.class);
    private static final int INPUT_TANK_CAPACITY = 1000; // 输入罐容量

    // ThreadLocal 变量：标记当前线程是否为玩家操作
    private static final ThreadLocal<Boolean> IS_PLAYER_OPERATION = ThreadLocal.withInitial(() -> false);
    // ThreadLocal 变量：标记当前是否为内部复制操作
    private static final ThreadLocal<Boolean> IS_INTERNAL_REPLICATION = ThreadLocal.withInitial(() -> false);

    private long outputCapacity; // 输出罐容量（根据机器等级变化）

    /**
     * 设置当前是否为玩家操作
     *
     * @param isPlayer true 表示当前是玩家操作，false 表示是非玩家操作（如管道）
     */
    public static void setPlayerOperation(boolean isPlayer) {
        IS_PLAYER_OPERATION.set(isPlayer);
    }

    /**
     * 检查当前是否为玩家操作
     *
     * @return true 表示当前是玩家操作，false 表示是非玩家操作（如管道）
     */
    public static boolean isPlayerOperation() {
        return IS_PLAYER_OPERATION.get();
    }

    /**
     * 设置是否是内部复制操作
     *
     * @param isInternal true 表示当前是内部复制操作，false 表示是外部操作
     */
    public static void setInternalReplication(boolean isInternal) {
        IS_INTERNAL_REPLICATION.set(isInternal);
    }

    /**
     * 检查是否是内部复制操作
     *
     * @return true 表示当前是内部复制操作，false 表示是外部操作
     */
    public static boolean isInternalReplication() {
        return IS_INTERNAL_REPLICATION.get();
    }

    // Tick 计数器，用于控制生产速度
    private int tickCounter = 0;
    // 机器等级
    public FluidReplicatorTier tier = FluidReplicatorTier.FLUID_TIER_1;
    // 当前存储的能量
    private long energyStored;
    // 最大能量容量
    private long energyCapacity;
    // 每次生产的能量消耗
    private long energyConsumption;

    // 初始化代码块
    {
        updateEnergyStats();
        updateOutputCapacity();
        energyStored = 0;
    }

    /**
     * 更新能量统计信息（容量和消耗）
     */
    private void updateEnergyStats() {
        switch (tier) {
            case FLUID_TIER_1:
                energyCapacity = ServerConfig.getFluidTier1EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier1EnergyConsumption();
                break;
            case FLUID_TIER_2:
                energyCapacity = ServerConfig.getFluidTier2EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier2EnergyConsumption();
                break;
            case FLUID_TIER_3:
                energyCapacity = ServerConfig.getFluidTier3EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier3EnergyConsumption();
                break;
            case FLUID_TIER_4:
                energyCapacity = ServerConfig.getFluidTier4EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier4EnergyConsumption();
                break;
            case FLUID_TIER_5:
                energyCapacity = ServerConfig.getFluidTier5EnergyCapacity();
                energyConsumption = ServerConfig.getFluidTier5EnergyConsumption();
                break;
        }
    }

    /**
     * 更新输出罐容量
     */
    private void updateOutputCapacity() {
        this.outputCapacity = switch (tier) {
            case FLUID_TIER_1 -> ServerConfig.getFluidTier1OutputTankCapacity();
            case FLUID_TIER_2 -> ServerConfig.getFluidTier2OutputTankCapacity();
            case FLUID_TIER_3 -> ServerConfig.getFluidTier3OutputTankCapacity();
            case FLUID_TIER_4 -> ServerConfig.getFluidTier4OutputTankCapacity();
            case FLUID_TIER_5 -> ServerConfig.getFluidTier5OutputTankCapacity();
        };
    }

    /**
     * 设置复制机的等级
     * @param tierId 等级 ID
     */
    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputCapacity();
    }

    /**
     * 能量处理器 - 处理流体能量的存储和提取
     */
    private final EnergyHandler energyHandler = new EnergyHandler() {
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

        @Override
        public int extract(int maxExtract, @NotNull TransactionContext transaction) {
            return 0; // 不支持能量提取
        }

        @Override
        public long getAmountAsLong() {
            return energyStored;
        }

        @Override
        public long getCapacityAsLong() {
            return energyCapacity;
        }
    };

    /**
     * 可清空的流体处理器 - 支持清空操作和输入罐保护
     */
    private class ClearableFluidHandler extends FluidStacksResourceHandler {
        // 是否允许清空模式
        private boolean allowClearing = false;
        // 输入罐容量
        private final int inputTankCapacity;
        
        public ClearableFluidHandler(int size, int capacity, int inputTankCapacity) {
            super(size, capacity);
            this.inputTankCapacity = inputTankCapacity;
        }
        
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
                }
                // 同种流体，合并
                else if (currentResource.equals(resource)) {
                    int canAdd = Math.min((int) outputCapacity - (int)currentAmount, maxAmount);
                    if (canAdd > 0) {
                        set(1, resource, (int)(currentAmount + canAdd));
                        markUpdated();
                    }
                    return canAdd;
                }
                return 0;
            }

            // 只能使用槽位 0（输入罐）
            if (slot != 0) {
                return 0;
            }

            // 判断是否是管道插入
            boolean isPipeInsertion = !isPlayerOperation();

            // 管道插入且开启销毁模式：模拟成功（实际销毁）
            if (isPipeInsertion && ServerConfig.isFluidReplicatorDestroyEnabled()) {
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
                int canAdd = Math.min(inputTankCapacity - (int)currentAmount, maxAmount);
                if (canAdd > 0) {
                    set(0, resource, (int)(currentAmount + canAdd));
                    markUpdated();
                }
                return canAdd;
            }
            
            return 0;
        }
        
        @Override
        public int extract(int slot, FluidResource resource, int amount, @NotNull TransactionContext transaction) {
            // 非清空模式下，只允许从输出罐抽取
            if (!allowClearing && slot != 1) {
                return 0;
            }
            return super.extract(slot, resource, amount, transaction);
        }

        /**
         * 清空所有流体
         * <p>
         * 功能：清空输入罐和输出罐
         *
         * <li><strong> 使用 allowClearing 标志位临时允许从输入罐抽取
         * <li><strong> 通过 set() 方法直接设置为空
         * <li><strong> 需要事务上下文（在 Transaction 中使用）
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
        
        @Override
        protected int getCapacity(int index, @NotNull FluidResource resource) {
            if (index == 0) return inputTankCapacity;
            if (index == 1) return (int) outputCapacity;
            return super.getCapacity(index, resource);
        }
    }

    // 流体处理器实例
    private final ClearableFluidHandler fluidHandler =
            new ClearableFluidHandler(2, (int) outputCapacity, INPUT_TANK_CAPACITY);

    /**
     * 清空所有流体
     * <p>
     * 功能：调用 fluidHandler.clearAll() 并标记更新
     *
     * <li><strong> 是 clearAll() 的封装层
     * <li><strong> 添加了 markUpdated() 用于同步到客户端
     * <li><strong> 供外部调用（如右键清空操作）
     */
    public void clearAllFluids() {
        fluidHandler.clearAll();
        markUpdated();
    }

    /**
     * 服务器端 Tick 方法 - 处理流体复制逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        // 未达到生产时间，跳过
        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            return;
        }
        
        blockEntity.tickCounter = 0;

        // 获取输入罐的流体
        FluidResource inputResource = blockEntity.fluidHandler.getResource(0);
        long inputAmount = blockEntity.fluidHandler.getAmountAsLong(0);

        // 没有输入流体，跳过
        if (inputResource.isEmpty() || inputAmount == 0) {
            return;
        }

        // 计算实际输出量
        int actualOutput = blockEntity.tier.getActualOutputAmount(inputResource.toStack((int) inputAmount));
        
        long energyPer1000MB = blockEntity.energyConsumption;
        long energyNeeded = (actualOutput * energyPer1000MB) / 1000;
        if (energyNeeded < 1 && actualOutput > 0) {
            energyNeeded = 1;
        }

        // 能量不足，跳过
        if (blockEntity.energyStored < energyNeeded) {
            return;
        }

        int remainingOutput = actualOutput;
        boolean hasUpdated = false;
        int totalOutput = 0;

        boolean autoOutputEnabled = ServerConfig.isFluidReplicatorAutoOutputEnabled();

        // 自动输出到相邻容器
        if (autoOutputEnabled) {
            Direction outputDirection = ServerConfig.getFluidReplicatorAutoOutputDirection();
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
                blockEntity.markUpdated();
            }
        }
    }

    @Override
    public void setLevel(@NotNull Level level) {
        super.setLevel(level);
        if (level.isClientSide()) {
            markUpdated();
        }
    }

    /**
     * 构造函数
     */
    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_REPLICATOR.get(), pos, state);
    }

    /**
     * 保存额外数据到 NBT
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
     * 获取更新标签用于客户端同步
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putLong("energyStored", energyStored);

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
        
        tag.put("tanks", tanksTag);

        return tag;
    }

    /**
     * 从 NBT 加载额外数据
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
    }

    /**
     * 从网络数据包加载流体数据（客户端）
     */
    public void loadFluidFromPacket(CompoundTag tag) {
        if (getLevel() != null && !getLevel().isClientSide()) {
            return;
        }

        CompoundTag tanksTag = tag.getCompound("tanks").orElse(new CompoundTag());
        
        clearFluidHandlerDirectly();

        // 加载输入罐
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
     * 直接清空流体处理器（不使用事务）
     * <p>
     * 功能：直接清空两个罐子
     *
     * <li><strong> 不使用事务，直接调用 set()
     * <li><strong> 仅用于客户端从网络包同步数据时
     * <li><strong> 避免客户端事务的复杂性
     */
    private void clearFluidHandlerDirectly() {
        fluidHandler.set(0, FluidResource.EMPTY, 0);
        fluidHandler.set(1, FluidResource.EMPTY, 0);
    }

    /**
     * 获取更新数据包
     */
    @Nullable
    @Override
    public Packet<@NotNull ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * 标记方块为已更新并发送同步包
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
     * 获取输入罐的流体
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
     * 获取输出罐的流体
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
     * 获取流体处理器
     */
    @Nullable
    public FluidStacksResourceHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    /**
     * 获取能量处理器
     */
    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    /**
     * 获取当前存储的能量
     */
    public long getEnergyStored() {
        return energyStored;
    }

    /**
     * 获取最大能量容量
     */
    public long getMaxEnergyStored() {
        return energyCapacity;
    }
}
