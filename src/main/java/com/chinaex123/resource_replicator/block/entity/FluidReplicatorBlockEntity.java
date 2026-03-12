package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.FluidReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.FluidReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class FluidReplicatorBlockEntity extends BlockEntity {
    private static final int INPUT_TANK_CAPACITY = 1000;

    /**
     * 输入流体储罐
     * <p>
     * 此储罐用于存储待复制的原料流体，容量由 INPUT_TANK_CAPACITY 定义。
     * 当罐内流体发生变化时会自动标记方块为已更新状态以同步到客户端。
     * 只允许通过黑白名单过滤的流体才能存入此罐。
     * </p>
     */
    private final FluidTank inputTank = new FluidTank(INPUT_TANK_CAPACITY) {
        /**
         * 内容物变化时的回调方法
         * <p>
         * 当流体被加入或取出时自动调用，用于触发方块数据同步。
         * </p>
         */
        @Override
        protected void onContentsChanged() {
            markUpdated();
        }

        /**
         * 检查流体是否有效（可放入）
         * <p>
         * 此方法在流体尝试进入储罐时被调用，会委托给 ReplicatorFilter 进行黑白名单检查。
         * 只有在白名单中或被黑名单允许的流体才能存入。
         * </p>
         *
         * @param stack 待检查的流体堆
         * @return boolean 如果该流体可以存入则返回 true，否则返回 false
         */
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return ReplicatorFilter.canInsertFluid(stack);
        }
    };

    /**
     * 输出流体储罐
     * <p>
     * 此储罐用于存储复制完成的产物流体，容量由当前等级的配置决定。
     * 当罐内流体发生变化时会自动标记方块为已更新状态以同步到客户端。
     * </p>
     */
    private final FluidTank outputTank = new FluidTank(ServerConfig.getFluidTier5OutputTankCapacity()) {

        // 当流体被加入或取出时自动调用，用于触发方块数据同步。
        @Override
        protected void onContentsChanged() {
            markUpdated();
        }
    };

    /**
     * 流体处理器接口实现
     * <p>
     * 此接口用于处理流体的输入输出操作，实现了 Mekanism 和其他模组的流体交互。
     * 包含两个储罐：输入罐（tank 0）和输出罐（tank 1）。
     * </p>
     */
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        /**
         * 获取可用储罐的总数量
         * 
         * @return int 固定返回 2（输入罐 + 输出罐）
         */
        @Override
        public int getTanks() {
            return 2;
        }

        /**
         * 获取指定储罐中的流体
         * 
         * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
         * @return FluidStack 指定储罐中的流体堆，如果索引无效或为空则返回 EMPTY
         */
        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank == 0) {
                return inputTank.getFluid();
            } else if (tank == 1) {
                return outputTank.getFluid();
            }
            return FluidStack.EMPTY;
        }

        /**
         * 获取指定储罐的最大容量
         * 
         * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
         * @return int 指定储罐的最大容量（单位：mB）
         */
        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? INPUT_TANK_CAPACITY : currentOutputTankCapacity;
        }

        /**
         * 检查流体是否可以放入指定储罐
         * 
         * @param tank 储罐索引（0 = 输入罐，1 = 输出罐）
         * @param stack 待检查的流体堆
         * @return boolean 只允许向输入罐（tank 0）放入流体，输出罐不允许直接插入
         */
        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 0;
        }

        /**
         * 向储罐注入流体
         * <p>
         * 此方法处理流体的输入逻辑，支持配置控制的管道销毁功能：
         * </p>
         * <ul>
         *     <li><strong>黑名单过滤</strong>：首先检查流体是否在黑名单中，如果是则拒绝</li>
         *     <li><strong>销毁功能开启</strong>：管道输入的流体会被瞬间销毁（返回实际数量）</li>
         *     <li><strong>销毁功能关闭</strong>：拒绝管道输入（返回 0）</li>
         *     <li><strong>玩家操作</strong>：正常存入输入罐</li>
         * </ul>
         * 
         * @param resource 要注入的流体堆
         * @param action 执行模式（EXECUTE = 实际执行，SIMULATE = 仅模拟）
         * @return int 实际注入的流体数量（mB），如果无法注入则返回 0
         */
        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            if (!ReplicatorFilter.canInsertFluid(resource)) {
                return 0;
            }

            // 检查是否是通过管道（非玩家操作）插入到输入槽的流体
            boolean isPipeInsertion = !isPlayerInsertion();

            // 如果是管道插入到输入槽
            if (isPipeInsertion) {
                if (ServerConfig.isFluidReplicatorDestroyEnabled()) {
                    // 启用销毁功能：瞬间销毁，返回实际数量表示全部"消耗"掉了
                    return resource.getAmount();
                } else {
                    // 未启用销毁功能：拒绝输入，返回 0 表示不接受任何流体
                    return 0;
                }
            }

            // 玩家操作：正常存入输入罐
            return inputTank.fill(resource, action);
        }

        /**
         * 从储罐抽取指定类型的流体
         * <p>
         * 只能从输出罐（tank 1）抽取与指定流体类型相同的流体。
         * </p>
         * 
         * @param resource 要抽取的流体类型和数量
         * @param action 执行模式（EXECUTE = 实际执行，SIMULATE = 仅模拟）
         * @return FluidStack 实际抽取到的流体堆，如果无法抽取则返回 EMPTY
         */
        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            if (!outputTank.isEmpty() &&
                    outputTank.getFluid().getFluidHolder().equals(resource.getFluidHolder())) {
                return outputTank.drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        /**
         * 从储罐抽取最大指定数量的流体
         * <p>
         * 只能从输出罐（tank 1）抽取流体，不限制流体类型。
         * </p>
         * 
         * @param maxDrain 最大可抽取的流体数量（单位：mB）
         * @param action 执行模式（EXECUTE = 实际执行，SIMULATE = 仅模拟）
         * @return FluidStack 实际抽取到的流体堆，如果输出罐为空则返回 EMPTY
         */
        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    };

    // 判断是否是玩家插入（通过检查调用栈）
    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 检查调用栈中是否包含 FluidReplicatorBlock 的 useItemOn 方法
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("FluidReplicatorBlock") &&
                    element.getMethodName().contains("useItemOn")) {
                return true;
            }
        }

        return false;
    }

    private int tickCounter = 0;
    private FluidReplicatorTier tier = FluidReplicatorTier.FLUID_TIER_1;
    private int energyStored;
    private int energyCapacity;
    private int energyConsumption;
    private int currentOutputTankCapacity;

    {
        updateEnergyStats();
        updateOutputTankCapacity();
        energyStored = 0;
    }

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

    private void updateOutputTankCapacity() {
        this.currentOutputTankCapacity = switch (tier) {
            case FLUID_TIER_1 -> ServerConfig.getFluidTier1OutputTankCapacity();
            case FLUID_TIER_2 -> ServerConfig.getFluidTier2OutputTankCapacity();
            case FLUID_TIER_3 -> ServerConfig.getFluidTier3OutputTankCapacity();
            case FLUID_TIER_4 -> ServerConfig.getFluidTier4OutputTankCapacity();
            case FLUID_TIER_5 -> ServerConfig.getFluidTier5OutputTankCapacity();
        };
    }

    /**
     * 能量处理器接口实现
     * <p>
     * 此接口用于处理能量的输入输出操作，实现了 NeoForge 的能量自动化交互。
     * </p>
     */
    private final IEnergyStorage energyHandler = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive()) {
                return 0;
            }
            int canReceive = Math.min(maxReceive, energyCapacity - energyStored);
            if (!simulate) {
                energyStored += canReceive;
                markUpdated();
            }
            return canReceive;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract()) {
                return 0;
            }
            int canExtract = Math.min(maxExtract, energyStored);
            if (!simulate) {
                energyStored -= canExtract;
                markUpdated();
            }
            return canExtract;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return energyCapacity;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public FluidReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_REPLICATOR.get(), pos, state);
    }

    /**
     * 保存方块实体的额外数据到 NBT 标签
     * <p>
     * 此方法在方块实体数据需要持久化时被调用（如世界保存、区块卸载等）。
     * 它会将当前 tick 计数器、机器等级以及输入/输出罐中的流体数据写入 NBT 标签，
     * 确保方块状态在重启世界后能够正确恢复。
     * </p>
     * 
     * @param tag 用于存储数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于序列化流体的注册表信息
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // 保存刻计数器和机器等级
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        // 创建流体罐数据标签并保存输入和输出罐的流体
        CompoundTag tanksTag = new CompoundTag();
        if (!inputTank.isEmpty()) {
            tanksTag.put("inputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        }
        if (!outputTank.isEmpty()) {
            tanksTag.put("outputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        }
        tag.put("tanks", tanksTag);
    }

    /**
     * 从 NBT 标签加载方块实体的数据
     * <p>
     * 此方法在方块实体数据需要从持久化存储中恢复时被调用（如世界加载、区块加载等）。
     * 它会从 NBT 标签中读取刻计数器、机器等级以及输入/输出罐中的流体数据，
     * 确保方块状态能够正确恢复到保存时的状态。如果标签中不存在 tier 字段，
     * 则保持默认等级；如果不存在流体罐数据，则将对应罐清空。
     * </p>
     * 
     * @param tag 包含方块实体数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于反序列化流体的注册表信息
     */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("tickCounter");
        if (tag.contains("tier")) {
            this.tier = FluidReplicatorTier.fromId(tag.getInt("tier"));
        }
        if (tag.contains("energyStored")) {
            this.energyStored = tag.getInt("energyStored");
        }

        CompoundTag tanksTag = tag.getCompound("tanks");
        if (tanksTag.contains("inputTank")) {
            inputTank.readFromNBT(registries, tanksTag.getCompound("inputTank"));
        } else {
            inputTank.setFluid(FluidStack.EMPTY);
        }
        if (tanksTag.contains("outputTank")) {
            outputTank.readFromNBT(registries, tanksTag.getCompound("outputTank"));
        } else {
            outputTank.setFluid(FluidStack.EMPTY);
        }

        updateEnergyStats();
        updateOutputTankCapacity();
    }

    /**
     * 获取用于同步方块实体数据到客户端的网络数据包
     * <p>
     * 此方法在服务器需要向客户端发送方块实体更新信息时被调用。
     * 它会创建一个客户端边界方块实体数据包，将当前的流体状态、刻计数器等信息
     * 同步给客户端，确保客户端渲染与服务端状态一致。
     * </p>
     * 
     * @return Packet<ClientGamePacketListener> 包含方块实体数据的网络包，用于客户端同步
     */
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * 获取用于同步方块实体数据到客户端的更新标签
     * <p>
     * 此方法在服务器需要向客户端发送方块实体更新信息时被调用。
     * 它会创建一个包含当前流体状态、刻计数器、机器等级等数据的 CompoundTag，
     * 用于通过网络同步到客户端，确保客户端显示与服务端一致。
     * </p>
     * 
     * @param registries 注册表查找提供器，用于序列化流体的注册表信息
     * @return CompoundTag 包含方块实体更新数据的复合标签
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        CompoundTag tanksTag = new CompoundTag();
        if (!inputTank.isEmpty()) {
            tanksTag.put("inputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        }
        if (!outputTank.isEmpty()) {
            tanksTag.put("outputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        }
        tag.put("tanks", tanksTag);

        return tag;
    }

    public void setTier(int tierId) {
        this.tier = FluidReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputTankCapacity();
    }

    /**
     * 服务器端刻更新逻辑
     * <p>
     * 此方法每刻在服务器端被调用一次，负责处理流体复制机的核心工作逻辑：
     * </p>
     * <ul>
     *     <li><strong>进度累加</strong>：每刻增加 tickCounter，达到处理速度时执行复制</li>
     *     <li><strong>输入检查</strong>：检查输入罐是否有流体，以及该流体是否可被当前等级复制</li>
     *     <li><strong>流体复制</strong>：根据等级配置的产量和速度，将输入流体复制到输出罐或相邻容器</li>
     *     <li><strong>自动输出</strong>：优先向周围六个方向的相邻容器输出产物流体</li>
     *     <li><strong>空间检查</strong>：检查输出罐和相邻容器的剩余空间</li>
     *     <li><strong>数据同步</strong>：复制完成后标记方块为已更新，同步状态到客户端</li>
     * </ul>
     * 
     * @param level 当前世界实例
     * @param pos 方块在世界中的坐标位置
     * @param state 方块的当前状态
     * @param blockEntity 流体复制机方块实体实例
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidReplicatorBlockEntity blockEntity) {
        // 累加刻计数器，用于控制复制速度
        blockEntity.tickCounter++;

        // 当刻计数器达到处理速度时，执行复制操作
        if (blockEntity.tickCounter >= blockEntity.tier.getProcessSpeed()) {
            // 获取输入罐的流体
            FluidStack inputFluid = blockEntity.inputTank.getFluid();

            // 如果输入罐不为空（有流体）
            if (!inputFluid.isEmpty()) {
                // 获取目标产出数量（不同等级产量不同）
                int actualOutput = blockEntity.tier.getOutputAmount();
                // 计算能量消耗 (每 1000mB 消耗 config 中定义的能量)
                int energyPer1000MB = blockEntity.energyConsumption;
                int energyNeeded = (actualOutput * energyPer1000MB) / 1000;
                if (energyNeeded < 1 && actualOutput > 0) {
                    energyNeeded = 1;
                }

                // ========== 预检查能量 ==========
                // 先检查是否有足够的能量支持至少 1 次输出
                if (blockEntity.energyStored < energyNeeded) {
                    return;
                }

                // 剩余待输出的流体数量（初始等于总产出量）
                int remainingOutput = actualOutput;
                // 标记是否有过更新操作
                boolean hasUpdated = false;
                // 记录总共输出了多少流体
                int totalOutput = 0;

                // 检查配置中是否启用了自动输出功能
                boolean autoOutputEnabled = ServerConfig.isFluidReplicatorAutoOutputEnabled();

                // 如果启用了自动输出
                if (autoOutputEnabled) {
                    // 从配置中获取输出方向
                    Direction outputDirection = ServerConfig.getFluidReplicatorAutoOutputDirection();

                    // 获取相邻方块的坐标
                    BlockPos neighborPos = pos.relative(outputDirection);

                    // 获取相邻方块的方块状态
                    BlockState neighborState = level.getBlockState(neighborPos);

                    // 如果相邻方块也是流体复制机，跳过（避免互相输入）
                    if (!(neighborState.getBlock() instanceof FluidReplicatorBlock)) {
                        // 获取相邻方块的流体处理能力（从相反方向访问）
                        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, neighborPos, outputDirection.getOpposite());

                        // 如果邻居有流体处理能力
                        if (handler != null) {
                            // 创建要输出的流体堆
                            FluidStack outputStack = new FluidStack(inputFluid.getFluid(), remainingOutput);
                            // 尝试向邻居注入流体，获取实际注入的量
                            int filled = handler.fill(outputStack, IFluidHandler.FluidAction.EXECUTE);

                            // 如果成功注入了流体
                            if (filled > 0) {
                                // 累加到总输出量
                                totalOutput += filled;
                                // 减少剩余待输出量
                                remainingOutput -= filled;
                                // 标记已更新
                                hasUpdated = true;
                            }
                        }
                    }
                }

                // ========== 将剩余流体存入输出罐 ==========
                // 如果还有剩余流体没有输出出去
                if (remainingOutput > 0) {
                    // 获取输出罐当前的流体
                    FluidStack currentOutput = blockEntity.outputTank.getFluid();

                    // 如果输出罐是空的
                    if (currentOutput.isEmpty()) {
                        // 创建新的流体堆放入输出罐
                        FluidStack newOutput = new FluidStack(inputFluid.getFluid(), remainingOutput);
                        blockEntity.outputTank.setFluid(newOutput);
                        // 累加到总输出量
                        totalOutput += remainingOutput;
                        // 标记已更新
                        hasUpdated = true;
                    }
                    // 如果输出罐中的流体与当前产出的流体类型相同
                    else if (currentOutput.getFluidHolder().equals(inputFluid.getFluidHolder())) {
                        // 计算输出罐的剩余空间
                        int spaceAvailable = blockEntity.outputTank.getCapacity() - currentOutput.getAmount();
                        // 计算实际能加入的量（取剩余空间和剩余产出量的较小值）
                        int toAdd = Math.min(spaceAvailable, remainingOutput);

                        // 如果有空间可以加入
                        if (toAdd > 0) {
                            // 增加输出罐中的流体数量
                            currentOutput.grow(toAdd);
                            // 累加到总输出量
                            totalOutput += toAdd;
                            // 标记已更新
                            hasUpdated = true;
                        }
                    }
                }

                // ========== 消耗能量并重置计数器 ==========
                // 如果有更新且总输出量大于 0
                if (hasUpdated && totalOutput > 0) {
                    // 计算实际消耗的能量（基于实际输出的总量）
                    int actualEnergyNeeded = (totalOutput * energyPer1000MB) / 1000;
                    // 确保至少消耗 1 FE
                    if (actualEnergyNeeded < 1) {
                        actualEnergyNeeded = 1;
                    }

                    // 再次检查能量是否足够
                    if (blockEntity.energyStored >= actualEnergyNeeded) {
                        // 扣除能量
                        blockEntity.energyStored -= actualEnergyNeeded;
                        // 重置刻计数器
                        blockEntity.tickCounter = 0;
                        // 标记方块为已更新状态，同步到客户端
                        blockEntity.markUpdated();
                    }
                }
            }
        }
    }

    /**
     * 清空所有流体
     * <p>
     * 此方法用于清除复制机输入罐和输出罐中的所有流体，通常在玩家使用空桶右键点击时调用。
     * 清空后会标记方块为已更新状态，确保客户端和服务端数据同步。
     * </p>
     */
    public void clearAllFluids() {
        inputTank.setFluid(FluidStack.EMPTY);
        outputTank.setFluid(FluidStack.EMPTY);
        markUpdated();
    }

    /**
     * 标记方块为已更新状态并同步到客户端
     * <p>
     * 此方法在方块实体的数据发生变化时被调用（如流体数量变化、进度更新等）。
     * 它会执行以下关键操作：
     * </p>
     * <ul>
     *     <li><strong>保存标记</strong>：调用 setChanged() 标记方块需要保存到磁盘</li>
     *     <li><strong>基础同步</strong>：调用 sendBlockUpdated() 通知世界方块已更新</li>
     *     <li><strong>主动推送</strong>：在服务端主动向周围 64 格内的所有玩家发送更新数据包</li>
     * </ul>
     * <p>
     * 这种双重同步机制确保了方块的数据既不会在世界重启后丢失，也能实时显示在客户端上。
     * 通过距离检测（64 格）优化了网络性能，只向附近的玩家发送更新。
     * </p>
     */
    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            if (!level.isClientSide) {
                var packet = ClientboundBlockEntityDataPacket.create(this);
                var players = level.players();
                for (var player : players) {
                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        double distance = Math.abs(serverPlayer.getX() - getBlockPos().getX()) +
                                Math.abs(serverPlayer.getY() - getBlockPos().getY()) +
                                Math.abs(serverPlayer.getZ() - getBlockPos().getZ());
                        if (distance < 64) {
                            serverPlayer.connection.send(packet);
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取输入罐中的流体
     * <p>
     * 此方法用于读取当前输入罐中的流体堆，可用于渲染或信息显示。
     * </p>
     * 
     * @return FluidStack 输入罐中的流体堆，如果为空则返回 EMPTY
     */
    public FluidStack getInputFluid() {
        return inputTank.getFluid();
    }

    /**
     * 获取输出罐中的流体
     * <p>
     * 此方法用于读取当前输出罐中的流体堆，可用于渲染或信息显示。
     * </p>
     * 
     * @return FluidStack 输出罐中的流体堆，如果为空则返回 EMPTY
     */
    public FluidStack getOutputFluid() {
        return outputTank.getFluid();
    }

    /**
     * 获取流体处理器接口
     * <p>
     * 此方法用于获取当前方块实体的 IFluidHandler 实现实例，该处理器负责处理流体的输入输出操作。
     * 返回的 fluidHandler 是一个懒加载的单例对象，在首次访问时创建。
     * </p>
     * 
     * @param side 方向参数，用于判断从哪个方向访问流体处理器（目前未使用，所有方向行为相同）
     * @return IFluidHandler 流体处理器实例
     */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }


    /**
     * 获取能量处理器接口
     * <p>
     * 此方法用于获取能量处理器接口，通常在能量自动化交互时调用。
     * </p>
     *
     * @param side 方块的朝向，如果为 null 则返回默认的能量处理器接口
     * @return IEnergyStorage 能量处理器接口的实现
     */
    @Nullable
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    /**
     * 获取当前存储的能量
     *
     * @return int 当前存储的能量值（FE）
     */
    public int getEnergyStored() {
        return energyStored;
    }

    /**
     * 获取最大能量存储
     *
     * @return int 最大能量存储（FE）
     */
    public int getMaxEnergyStored() {
        return energyCapacity;
    }
}
