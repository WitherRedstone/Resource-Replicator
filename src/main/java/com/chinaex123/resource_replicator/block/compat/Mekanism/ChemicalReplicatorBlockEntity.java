package com.chinaex123.resource_replicator.block.compat.Mekanism;

import com.chinaex123.resource_replicator.block.entity.ModBlockEntities;
import com.chinaex123.resource_replicator.config.ServerConfig;
import mekanism.api.Action;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 化学品复制机实体 - 包含 Mekanism API（仅在 Mekanism 安装时加载）
 */
public class ChemicalReplicatorBlockEntity extends BlockEntity {
    static final int INPUT_TANK_CAPACITY = 1000;

    // 化学品储罐
    ChemicalStack inputChemical = ChemicalStack.EMPTY;
    ChemicalStack outputChemical = ChemicalStack.EMPTY;

    int tickCounter = 0;
    int tier = 1;
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
            case 1:
                energyCapacity = ServerConfig.getChemicalTier1EnergyCapacity();
                energyConsumption = ServerConfig.getChemicalTier1EnergyConsumption();
                break;
            case 2:
                energyCapacity = ServerConfig.getChemicalTier2EnergyCapacity();
                energyConsumption = ServerConfig.getChemicalTier2EnergyConsumption();
                break;
            case 3:
                energyCapacity = ServerConfig.getChemicalTier3EnergyCapacity();
                energyConsumption = ServerConfig.getChemicalTier3EnergyConsumption();
                break;
            case 4:
                energyCapacity = ServerConfig.getChemicalTier4EnergyCapacity();
                energyConsumption = ServerConfig.getChemicalTier4EnergyConsumption();
                break;
            case 5:
                energyCapacity = ServerConfig.getChemicalTier5EnergyCapacity();
                energyConsumption = ServerConfig.getChemicalTier5EnergyConsumption();
                break;
        }
    }

    private void updateOutputTankCapacity() {
        this.currentOutputTankCapacity = switch (tier) {
            case 1 -> ServerConfig.getChemicalTier1OutputTankCapacity();
            case 2 -> ServerConfig.getChemicalTier2OutputTankCapacity();
            case 3 -> ServerConfig.getChemicalTier3OutputTankCapacity();
            case 4 -> ServerConfig.getChemicalTier4OutputTankCapacity();
            case 5 -> ServerConfig.getChemicalTier5OutputTankCapacity();
            default -> ServerConfig.getChemicalTier1OutputTankCapacity();
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
            int canReceive = Math.min(maxReceive, getMaxEnergyStored() - getEnergyStored());
            if (!simulate && canReceive > 0) {
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
            int canExtract = Math.min(maxExtract, getEnergyStored());
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

    // 懒加载的化学品处理器
    @Nullable
    private Object chemicalHandler = null;

    public ChemicalReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REPLICATOR.get(), pos, state);
    }

    /**
     * 保存方块实体的额外数据到 NBT 标签
     * <p>
     * 此方法在方块实体数据需要持久化时被调用（如世界保存、区块卸载等）。
     * 它会将当前 tick 计数器、机器等级以及输入/输出罐中的化学品数据写入 NBT 标签，
     * 确保方块状态在重启世界后能够正确恢复。
     * 
     * @param tag 用于存储数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于序列化化学品的注册表信息
     */
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        // 保存刻计数器和机器等级
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier);
        tag.putInt("energyStored", energyStored);

        // 创建化学品数据标签并保存输入和输出罐的化学品
        CompoundTag chemicalsTag = new CompoundTag();
        if (!inputChemical.isEmpty()) {
            chemicalsTag.put("inputChemical", writeChemicalToNBT(inputChemical, registries));
        }
        if (!outputChemical.isEmpty()) {
            chemicalsTag.put("outputChemical", writeChemicalToNBT(outputChemical, registries));
        }
        tag.put("chemicals", chemicalsTag);
    }

    /**
     * 从 NBT 标签加载方块实体的数据
     * <p>
     * 此方法在方块实体数据需要从持久化存储中恢复时被调用（如世界加载、区块加载等）。
     * 它会从 NBT 标签中读取刻计数器、机器等级以及输入/输出罐中的化学品数据，
     * 确保方块状态能够正确恢复到保存时的状态。
     * 
     * @param compound 包含方块实体数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于反序列化化学品的注册表信息
     */
    @Override
    public void loadAdditional(@NotNull CompoundTag compound, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(compound, registries);

        CompoundTag chemicalsTag = compound.getCompound("chemicals");
        inputChemical = readChemicalFromNBT(chemicalsTag.getCompound("inputChemical"), registries);
        outputChemical = readChemicalFromNBT(chemicalsTag.getCompound("outputChemical"), registries);

        tickCounter = compound.getInt("tickCounter");
        int loadedTier = compound.getInt("tier");
        if (loadedTier < 1 || loadedTier > 5) {
            loadedTier = 1;
        }
        this.tier = loadedTier;

        if (compound.contains("energyStored")) {
            this.energyStored = compound.getInt("energyStored");
        }

        updateEnergyStats();
        updateOutputTankCapacity();
    }

    /**
     * 处理客户端接收到的更新标签
     * <p>
     * 此方法在客户端接收到服务端发送的方块实体更新数据包时被调用。
     * 它会从更新标签中读取最新的化学品数据并同步到客户端的方块实体中，
     * 确保客户端显示的化学品状态与服务端保持一致。
     * 
     * @param tag 包含更新数据的 CompoundTag 对象
     * @param lookupProvider 注册表查找提供器，用于反序列化化学品的注册表信息
     */
    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);

        CompoundTag chemicalsTag = tag.getCompound("chemicals");
        inputChemical = readChemicalFromNBT(chemicalsTag.getCompound("inputChemical"), lookupProvider);
        outputChemical = readChemicalFromNBT(chemicalsTag.getCompound("outputChemical"), lookupProvider);

        if (tag.contains("tier")) {
            this.tier = tag.getInt("tier");
        }
        if (tag.contains("energyStored")) {
            this.energyStored = tag.getInt("energyStored");
        }
        if (tag.contains("energyCapacity")) {
            this.energyCapacity = tag.getInt("energyCapacity");
        }

        updateEnergyStats();
        updateOutputTankCapacity();
    }

    /**
     * 获取用于同步方块实体数据到客户端的网络数据包
     * <p>
     * 此方法在服务器需要向客户端发送方块实体更新信息时被调用。
     * 它会创建一个客户端边界方块实体数据包，将当前的化学品状态、刻计数器等信息
     * 同步给客户端，确保客户端渲染与服务端状态一致。
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
     * 它会创建一个包含当前化学品状态、刻计数器等数据的 CompoundTag，
     * 用于通过网络同步到客户端，确保客户端显示与服务端一致。
     * 
     * @param registries 注册表查找提供器，用于序列化化学品的注册表信息
     * @return CompoundTag 包含方块实体更新数据的复合标签
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier);
        tag.putInt("energyStored", energyStored);
        tag.putInt("energyCapacity", energyCapacity);

        CompoundTag chemicalsTag = new CompoundTag();
        if (!inputChemical.isEmpty()) {
            chemicalsTag.put("inputChemical", writeChemicalToNBT(inputChemical, registries));
        }
        if (!outputChemical.isEmpty()) {
            chemicalsTag.put("outputChemical", writeChemicalToNBT(outputChemical, registries));
        }
        tag.put("chemicals", chemicalsTag);

        return tag;
    }

    public void setTier(int tierId) {
        this.tier = tierId;
        updateEnergyStats();
        updateOutputTankCapacity();
    }

    public int getTier() {
        return tier;
    }

    /**
     * 获取当前等级的输出罐容量
     * 
     * @return int 当前等级的输出罐容量（单位：mB）
     */
    public int getCurrentOutputTankCapacity() {
        return currentOutputTankCapacity;
    }

    /**
     * 服务器端刻更新逻辑
     * <p>
     * 此方法每刻在服务器端被调用一次，负责处理化学品复制机的核心工作逻辑：
     * </p>
     * <ul>
     *     <li><strong>进度累加</strong>：每刻增加 tickCounter，达到处理速度时执行复制</li>
     *     <li><strong>输入检查</strong>：检查输入罐是否有化学品，以及该化学品是否可被当前等级复制</li>
     *     <li><strong>空间检查</strong>：检查输出罐是否有足够空间容纳新产生的化学品</li>
     *     <li><strong>化学品复制</strong>：根据等级配置的产量和速度，将输入化学品复制到输出罐</li>
     *     <li><strong>数据同步</strong>：复制完成后标记方块为已更新，同步状态到客户端</li>
     * </ul>
     */
    public void serverTick() {
        tickCounter++;

        // 如果输入罐不为空，开始处理逻辑
        if (!inputChemical.isEmpty()) {
            // 根据当前等级 ID 获取对应的复制机等级枚举
            ChemicalReplicatorTier tier = ChemicalReplicatorTier.fromId(this.tier);

            // 检查当前等级的复制机是否可以复制该化学品
            if (!tier.canReplicateChemical(inputChemical.getChemical())) {
                return;
            }

            // 获取当前等级的实际处理速度（tick 数）
            int actualSpeed = tier.getActualProcessSpeed(inputChemical.getChemical());

            // 如果刻计数器达到处理速度，执行复制操作
            if (tickCounter >= actualSpeed) {
                int actualOutput = tier.getActualOutputAmount(inputChemical.getChemical());

                // 计算能量消耗 (每 1000mB 消耗 config 中定义的能量)
                int energyPer1000MB = energyConsumption;
                int energyNeeded = (int)((actualOutput * energyPer1000MB) / 1000L);
                if (energyNeeded < 1 && actualOutput > 0) {
                    energyNeeded = 1;
                }

                // 检查是否有足够的能量
                if (energyStored < energyNeeded) {
                    return;
                }

                if (!outputChemical.isEmpty()) {
                    // 如果输出罐不为空，检查是否是同种化学品
                    if (outputChemical.getChemical() != inputChemical.getChemical()) {
                        return;
                    }
                    // 计算输出罐剩余空间
                    long spaceInOutput = currentOutputTankCapacity - outputChemical.getAmount();
                    // 如果剩余空间不足，等待下次处理
                    if (spaceInOutput < actualOutput) {
                        return;
                    }
                    // 增加输出罐中的化学品数量
                    outputChemical = outputChemical.copyWithAmount(
                            outputChemical.getAmount() + actualOutput
                    );
                } else {
                    // 输出罐为空，创建新的化学品堆
                    var chemical = inputChemical.getChemical();
                    // 将化学品包装为 Holder 对象
                    var holder = MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical);
                    // 创建新的化学品堆并设置到输出罐
                    outputChemical = new ChemicalStack(holder, actualOutput);
                }

                // 计算实际消耗的能量
                int actualEnergyNeeded = (int)((actualOutput * energyPer1000MB) / 1000L);
                if (actualEnergyNeeded < 1 && actualOutput > 0) {
                    actualEnergyNeeded = 1;
                }

                if (energyStored >= actualEnergyNeeded) {
                    energyStored -= actualEnergyNeeded;
                    tickCounter = 0;

                    // 自动输出逻辑
                    if (ServerConfig.isChemicalReplicatorAutoOutputEnabled() && !outputChemical.isEmpty()) {
                        autoOutputChemical();
                    }

                    markUpdated();
                }
            }
        }
    }

    /**
     * 自动输出化学品到相邻的容器
     * <p>
     * 此方法尝试将输出罐中的化学品输出到指定方向的相邻容器中。
     * 只有当配置文件中启用了自动输出功能时才会执行。
     * </p>
     */
    private void autoOutputChemical() {
        if (level == null || level.isClientSide || outputChemical.isEmpty()) {
            return;
        }

        // 从配置中获取输出方向
        Direction outputDirection = ServerConfig.getChemicalReplicatorAutoOutputDirection();

        // 获取相邻方块的坐标
        BlockPos neighborPos = worldPosition.relative(outputDirection);

        // 获取相邻方块的 BlockEntity
        BlockEntity neighborBE = level.getBlockEntity(neighborPos);
        if (neighborBE == null) {
            return;
        }

        // 检查邻居是否实现了 IChemicalHandler 接口
        if (neighborBE instanceof IChemicalHandler handler) {
            // 调用 insertChemical 方法（tank 0 是输入罐，接受外部输入）
            ChemicalStack remainder = handler.insertChemical(0, outputChemical, Action.EXECUTE);

            // 如果成功插入，减少输出罐中的化学品数量
            if (remainder.getAmount() < outputChemical.getAmount()) {
                long inserted = outputChemical.getAmount() - remainder.getAmount();

                outputChemical = outputChemical.copyWithAmount(outputChemical.getAmount() - inserted);

                if (outputChemical.getAmount() <= 0) {
                    outputChemical = ChemicalStack.EMPTY;
                }

                markUpdated();
            }
        }
    }

    /**
     * 检查是否有输入化学品
     * <p>
     * 此方法用于判断复制机的输入罐是否包含化学品。当输入罐不为空时返回 true，
     * 否则返回 false。可用于外部逻辑判断机器是否正在工作或需要添加原料。
     *
     * @return boolean 如果输入罐不为空则返回 true，否则返回 false
     */
    public boolean hasInput() {
        return !inputChemical.isEmpty();
    }

    /**
     * 获取化学品的翻译键名称
     * <p>
     * 此方法用于获取当前输入罐中化学品的翻译键，可用于本地化显示或消息提示。
     * 如果输入罐为空，则返回 "Empty" 字符串。
     * 
     * @return String 化学品的翻译键（如 "chemical.mekanism.hydrogen"），如果输入罐为空则返回 "Empty"
     */
    public String getChemicalName() {
        if (!inputChemical.isEmpty()) {
            return inputChemical.getChemical().getTranslationKey();
        }
        return "Empty";
    }

    /**
     * 清空所有化学品
     * <p>
     * 此方法用于清除复制机输入罐和输出罐中的所有化学品，通常在玩家使用空储罐右键点击时调用。
     * 清空后会标记方块为已更新状态，确保客户端和服务端数据同步。
     */
    public void clearAllChemicals() {
        inputChemical = ChemicalStack.EMPTY;
        outputChemical = ChemicalStack.EMPTY;
        markUpdated();
    }

    /**
     * 获取化学品处理器
     * <p>
     * 此方法用于获取当前方块实体的 IChemicalHandler 实现实例，该处理器负责处理化学品的输入输出操作。
     * 使用懒加载模式创建处理器实例，通过反射机制避免编译时对 Mekanism API 的直接依赖。
     * 首次调用时会创建 ChemicalReplicatorHandler 实例并缓存，后续调用直接返回缓存的实例。
     * </p>
     * 
     * @param side 方向参数，用于判断从哪个方向访问化学品处理器（目前未使用）
     * @return Object 化学品处理器实例，如果创建失败则返回 null
     */
    @Nullable
    public Object getChemicalHandler(@Nullable Direction side) {
        if (chemicalHandler == null) {
            try {
                // 懒加载创建 IChemicalHandler 实现类
                Class<?> handlerClass = Class.forName("com.chinaex123.resource_replicator.block.compat.Mekanism.ChemicalReplicatorHandler");
                chemicalHandler = handlerClass.getDeclaredConstructor(ChemicalReplicatorBlockEntity.class)
                        .newInstance(this);
            } catch (Exception e) {
                return null;
            }
        }
        return chemicalHandler;
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
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    // 添加用于渲染的方法，返回 Object 避免编译时依赖
    @Nullable
    public Object getInputChemical() {
        return inputChemical;
    }

    /**
     * 标记方块为已更新状态并同步到客户端
     * <p>
     * 此方法在方块实体的数据发生变化时被调用（如化学品数量变化、进度更新等）。
     * 它会执行两个关键操作：
     * </p>
     * <ul>
     *     <li><strong>保存标记</strong>：调用 setChanged() 标记方块需要保存到磁盘</li>
     *     <li><strong>网络同步</strong>：在服务端调用 sendBlockUpdated() 将最新状态同步给所有观察到此方块的客户端</li>
     * </ul>
     * 这确保了方块的数据既不会在世界重启后丢失，也能实时显示在客户端上。
     */
    void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 将化学品堆写入 NBT 标签
     * <p>
     * 此方法用于将化学品堆的数据序列化为 CompoundTag 格式，以便保存到世界数据或通过网络同步。
     * 它会写入化学品的数量（amount）和注册表 ID（id），如果化学品堆为空则返回空标签。
     * </p>
     * 
     * @param stack 要序列化的化学品堆对象
     * @param registries 注册表查找提供器，用于获取化学品的注册表位置
     * @return CompoundTag 包含化学品数据的复合标签，格式为 {amount: Long, chemical: {id: String}}
     */
    private CompoundTag writeChemicalToNBT(ChemicalStack stack, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            tag.putLong("amount", stack.getAmount());
            CompoundTag chemicalTag = new CompoundTag();
            ResourceLocation chemicalId = MekanismAPI.CHEMICAL_REGISTRY.getKey(stack.getChemical());
            chemicalTag.putString("id", chemicalId.toString());
            tag.put("chemical", chemicalTag);
        }
        return tag;
    }

    /**
     * 从 NBT 标签读取化学品堆
     * <p>
     * 此方法用于将 CompoundTag 格式的数据反序列化为化学品堆对象，以便从世界数据或网络包中恢复化学品信息。
     * 它会从标签中读取化学品的注册表 ID 和数量，并尝试从 Mekanism 注册表中查找对应的化学品。
     * 如果标签格式不正确、找不到对应的化学品或发生异常，则返回空的化学品堆。
     * 
     * @param tag 包含化学品数据的复合标签，期望格式为 {amount: Long, chemical: {id: String}}
     * @param registries 注册表查找提供器，用于查找化学品的 Holder 引用
     * @return ChemicalStack 反序列化后的化学品堆对象，如果加载失败则返回 ChemicalStack.EMPTY
     */
    private ChemicalStack readChemicalFromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("chemical") || !tag.contains("amount")) {
            return ChemicalStack.EMPTY;
        }

        long amount = tag.getLong("amount");
        CompoundTag chemicalTag = tag.getCompound("chemical");

        if (!chemicalTag.contains("id", 8)) {
            return ChemicalStack.EMPTY;
        }

        String chemicalId = chemicalTag.getString("id");

        try {
            ResourceLocation location = ResourceLocation.parse(chemicalId);
            Optional<Holder.Reference<Chemical>> holderOpt = MekanismAPI.CHEMICAL_REGISTRY.holders()
                    .filter(h -> h.unwrapKey().map(key -> key.location().equals(location)).orElse(false))
                    .findFirst();
            if (holderOpt.isPresent()) {
                return new ChemicalStack(holderOpt.get(), amount);
            }
        } catch (Exception e) {
            MekanismAPI.logger.warn("[化学品复制机]从 NBT 加载化学品失败：{}", chemicalId);
        }

        return ChemicalStack.EMPTY;
    }
}
