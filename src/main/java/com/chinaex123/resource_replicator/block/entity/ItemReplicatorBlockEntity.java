package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.ItemReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.RRServerConfig;
import com.chinaex123.resource_replicator.init.RRBlockEntities;
import com.chinaex123.resource_replicator.network.ItemReplicatorSyncPacket;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 * 物品复制机方块实体。
 * <p>
 * 负责处理输入物品的存储、按 tier 配置进行物品复制、
 * 输出到相邻容器或自身输出槽、能量消耗以及客户端同步。
 */
public class ItemReplicatorBlockEntity extends BlockEntity {

    /**
     * 输入槽索引。
     */
    private static final int INPUT_SLOT = 0;

    /**
     * 输出槽起始索引。
     */
    private static final int OUTPUT_SLOT_START = 1;

    /**
     * 最大输出槽数量。
     */
    private static final int MAX_OUTPUT_SLOTS = 9;

    /**
     * 总槽位数量（输入槽 + 输出槽 + 虚拟槽）。
     */
    private static final int TOTAL_SLOTS = 1 + MAX_OUTPUT_SLOTS + 1;

    /**
     * 虚拟槽索引，用于标记当前正在复制的物品。
     */
    private static final int VIRTUAL_SLOT = 10;

    /**
     * 物品槽数组。
     */
    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];

    /**
     * tick 计数器，用于控制处理速度。
     */
    private int tickCounter = 0;

    /**
     * 当前 tier。
     */
    private ItemReplicatorTier tier = ItemReplicatorTier.ITEM_TIER_1;

    /**
     * 当前存储的能量。
     */
    private int energyStored;

    /**
     * 能量容量，由 tier 配置决定。
     */
    private int energyCapacity;

    /**
     * 每次复制消耗的能量，由 tier 配置决定。
     */
    private int energyConsumption;

    /**
     * 当前输出槽数量，由 tier 配置决定。
     */
    private int currentOutputSlots;

    /**
     * 虚拟槽是否激活。
     */
    private boolean virtualSlotActive = false;

    /**
     * 虚拟槽物品资源。
     */
    private ItemResource virtualSlotResource = ItemResource.EMPTY;

    /**
     * 虚拟槽物品数量，默认为无限。
     */
    private long virtualSlotAmount = Long.MAX_VALUE;

    /**
     * 虚拟槽累加器，用于按 tick 累积生产进度。
     */
    private int virtualSlotAccumulator = 0;

    /**
     * 每 tick 生产速率。
     */
    private int productionRatePerTick;

    {
        updateEnergyStats();
        energyStored = 0;
        updateOutputSlots();
        items[VIRTUAL_SLOT] = ItemStack.EMPTY;
        updateProductionRate();
    }

    /**
     * 根据当前 tier 更新能量容量与能耗配置。
     */
    private void updateEnergyStats() {
        switch (tier) {
            case ITEM_TIER_1:
                energyCapacity = RRServerConfig.ITEM_TIER1_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.ITEM_TIER1_ENERGY_CONSUMPTION.get();
                break;
            case ITEM_TIER_2:
                energyCapacity = RRServerConfig.ITEM_TIER2_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.ITEM_TIER2_ENERGY_CONSUMPTION.get();
                break;
            case ITEM_TIER_3:
                energyCapacity = RRServerConfig.ITEM_TIER3_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.ITEM_TIER3_ENERGY_CONSUMPTION.get();
                break;
            case ITEM_TIER_4:
                energyCapacity = RRServerConfig.ITEM_TIER4_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.ITEM_TIER4_ENERGY_CONSUMPTION.get();
                break;
            case ITEM_TIER_5:
                energyCapacity = RRServerConfig.ITEM_TIER5_ENERGY_CAPACITY.get();
                energyConsumption = RRServerConfig.ITEM_TIER5_ENERGY_CONSUMPTION.get();
                break;
        }
    }

    /**
     * 根据当前 tier 更新输出槽数量。
     */
    private void updateOutputSlots() {
        this.currentOutputSlots = switch (tier) {
            case ITEM_TIER_1 -> RRServerConfig.ITEM_TIER1_OUTPUT_SLOTS.get();
            case ITEM_TIER_2 -> RRServerConfig.ITEM_TIER2_OUTPUT_SLOTS.get();
            case ITEM_TIER_3 -> RRServerConfig.ITEM_TIER3_OUTPUT_SLOTS.get();
            case ITEM_TIER_4 -> RRServerConfig.ITEM_TIER4_OUTPUT_SLOTS.get();
            case ITEM_TIER_5 -> RRServerConfig.ITEM_TIER5_OUTPUT_SLOTS.get();
        };
    }

    /**
     * 根据当前 tier 更新每 tick 生产速率。
     */
    private void updateProductionRate() {
        int outputAmount = tier.getOutputAmount();
        int processSpeed = tier.getProcessSpeed();

        this.productionRatePerTick = (int) Math.floor((double) outputAmount / processSpeed);
        if (this.productionRatePerTick < 1) {
            this.productionRatePerTick = 1;
        }
    }

    /**
     * 物品资源处理器。
     * <p>
     * 槽位 0 为输入槽，槽位 1~9 为输出槽，槽位 10 为虚拟槽。
     * 输入槽仅允许玩家插入或配置允许时接受管道插入；
     * 输出槽允许抽出；虚拟槽仅用于标记复制目标。
     */
    private final ResourceHandler<@NotNull ItemResource> itemHandler = new ResourceHandler<>() {
        /**
         * 获取槽位数量。
         *
         * @return 总槽位数量
         */
        @Override
        public int size() {
            return TOTAL_SLOTS;
        }

        /**
         * 获取指定槽位的物品资源。
         *
         * @param index 槽位索引
         * @return 物品资源，空则为 ItemResource.EMPTY
         */
        @Override
        public @NonNull ItemResource getResource(int index) {
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                return virtualSlotResource;
            }
            if (index >= 0 && index < TOTAL_SLOTS) {
                ItemStack stack = items[index];
                if (!stack.isEmpty()) {
                    return ItemResource.of(stack);
                }
            }
            return ItemResource.EMPTY;
        }

        /**
         * 获取指定槽位的物品数量。
         *
         * @param index 槽位索引
         * @return 物品数量
         */
        @Override
        public long getAmountAsLong(int index) {
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                return virtualSlotAmount;
            }
            if (index >= 0 && index < TOTAL_SLOTS) {
                ItemStack stack = items[index];
                return stack.isEmpty() ? 0L : stack.getCount();
            }
            return 0L;
        }

        /**
         * 获取指定槽位的容量。
         *
         * @param index    槽位索引
         * @param resource 物品资源
         * @return 槽位容量
         */
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index == VIRTUAL_SLOT) {
                return Long.MAX_VALUE;
            }
            return 64L;
        }

        /**
         * 向指定槽位插入物品。
         * <p>
         * 仅输入槽（槽位 0）允许插入。
         * 管道插入时，若配置允许销毁则直接消耗，否则拒绝。
         * 玩家插入时正常写入输入槽。
         *
         * @param index       槽位索引
         * @param resource    物品资源
         * @param amount      最大插入量
         * @param transaction 事务上下文
         * @return 实际插入量
         */
        @Override
        public int insert(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
            if (index == VIRTUAL_SLOT) {
                return 0;
            }

            if (index == INPUT_SLOT) {
                if (resource.isEmpty()) {
                    return 0;
                }

                boolean isPipeInsertion = !isPlayerInsertion();

                if (isPipeInsertion) {
                    if (RRServerConfig.ITEM_REPLICATOR_ENABLE_DESTROY.get()) {
                        return amount;
                    } else {
                        return 0;
                    }
                }

                ItemStack stack = resource.toStack(1);
                ItemStack current = items[index];

                if (current.isEmpty()) {
                    int toInsert = Math.min(amount, stack.getMaxStackSize());
                    items[index] = stack.copyWithCount(toInsert);
                    markUpdated();
                    return toInsert;
                } else if (ItemStack.isSameItemSameComponents(current, stack)) {
                    int canAdd = Math.min(stack.getMaxStackSize() - current.getCount(), amount);
                    if (canAdd > 0) {
                        items[index] = current.copyWithCount(current.getCount() + canAdd);
                        markUpdated();
                    }
                    return canAdd;
                }
                return 0;
            }
            return 0;
        }

        /**
         * 从指定槽位抽出物品。
         * <p>
         * 虚拟槽抽出时消耗能量并返回复制产物；
         * 输入槽不允许抽出；
         * 输出槽正常抽出物品。
         *
         * @param index       槽位索引
         * @param resource    物品资源
         * @param amount      抽出量
         * @param transaction 事务上下文
         * @return 实际抽出量
         */
        @Override
        public int extract(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                if (energyStored < energyConsumption) {
                    virtualSlotActive = false;
                    virtualSlotResource = ItemResource.EMPTY;
                    virtualSlotAmount = Long.MAX_VALUE;
                    virtualSlotAccumulator = 0;
                    markUpdated();
                    return 0;
                }

                if (!resource.equals(virtualSlotResource)) {
                    return 0;
                }

                int maxExtract = tier.getOutputAmount();
                int toExtract = Math.min(amount, maxExtract);

                if (toExtract > 0) {
                    energyStored -= energyConsumption;
                    virtualSlotAmount = Long.MAX_VALUE;
                    markUpdated();
                    return toExtract;
                }
                return 0;
            }

            // 输入槽不允许抽取
            if (index == INPUT_SLOT) {
                return 0;
            }

            // 真实输出槽的处理
            if (index >= OUTPUT_SLOT_START && index < OUTPUT_SLOT_START + MAX_OUTPUT_SLOTS) {
                ItemStack current = items[index];

                if (current.isEmpty()) {
                    return 0;
                }

                if (!ItemStack.isSameItemSameComponents(current, resource.toStack(1))) {
                    return 0;
                }

                int toExtract = Math.min(amount, current.getCount());

                if (toExtract <= 0) {
                    return 0;
                }

                ItemStack remaining = current.copyWithCount(current.getCount() - toExtract);
                items[index] = remaining.isEmpty() ? ItemStack.EMPTY : remaining;

                markUpdated();

                return toExtract;
            }

            return 0;
        }

        /**
         * 判断指定槽位是否接受该物品资源。
         *
         * @param index    槽位索引
         * @param resource 物品资源
         * @return 是否有效
         */
        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (index == INPUT_SLOT) {
                ItemStack stack = resource.toStack(1);
                return ReplicatorFilter.canInsertItem(stack);
            }
            return true;
        }
    };

    /**
     * 判断当前插入操作是否由玩家触发。
     * <p>
     * 通过检查调用栈中是否包含 ItemReplicatorBlock 的 useWithoutItem 方法来判断。
     *
     * @return 是否为玩家插入
     */
    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("ItemReplicatorBlock") &&
                    element.getMethodName().contains("useWithoutItem")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 构造物品复制机方块实体。
     *
     * @param pos   方块位置
     * @param state 方块状态
     */
    public ItemReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(RRBlockEntities.ITEM_REPLICATOR.get(), pos, state);
        Arrays.fill(items, ItemStack.EMPTY);
    }

    /**
     * 保存附加数据到 NBT。
     * <p>
     * 包含 tickCounter、tier、能量、虚拟槽状态以及所有物品槽内容。
     *
     * @param output 数据输出
     */
    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);

        CompoundTag customData = new CompoundTag();
        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putInt("energyStored", energyStored);
        customData.putBoolean("virtualSlotActive", virtualSlotActive);

        if (virtualSlotActive && !virtualSlotResource.isEmpty()) {
            ItemStack virtualStack = virtualSlotResource.toStack(1);
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, virtualStack)
                    .result()
                    .ifPresent(tag -> customData.put("virtualSlotResourceStack", tag));
        }

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int finalI = i;
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, items[i])
                        .result()
                        .ifPresent(tag -> itemsTag.put("item" + finalI, tag));
            }
        }
        customData.put("items", itemsTag);

        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    /**
     * 从 NBT 加载附加数据。
     * <p>
     * 恢复 tickCounter、tier、能量、虚拟槽状态以及所有物品槽内容，
     * 并刷新能量与输出槽配置。
     *
     * @param input 数据输入
     */
    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);

        CompoundTag customData = input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());
        virtualSlotActive = customData.getBoolean("virtualSlotActive").orElse(false);

        if (virtualSlotActive && customData.contains("virtualSlotResourceStack")) {
            customData.getCompound("virtualSlotResourceStack")
                    .flatMap(tag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).result())
                    .ifPresent(stack -> {
                        if (!stack.isEmpty()) {
                            virtualSlotResource = ItemResource.of(stack);
                            virtualSlotAmount = Long.MAX_VALUE;
                        }
                    });
        }

        tickCounter = customData.getInt("tickCounter").orElse(0);
        if (customData.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(customData.getInt("tier").orElse(0));
        }
        if (customData.contains("energyStored")) {
            this.energyStored = customData.getInt("energyStored").orElse(0);
        }

        updateEnergyStats();
        updateOutputSlots();

        CompoundTag itemsTag = customData.getCompound("items").orElse(new CompoundTag());

        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            int finalI = i;

            itemsTag.getCompound(key).flatMap(itemTag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag)
                    .result()).ifPresent(stack -> items[finalI] = stack);
        }

        setChanged();
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
     * 处理客户端更新标签。
     * <p>
     * 清空本地数据后，从数据中恢复 tickCounter、tier、能量、
     * 虚拟槽状态以及所有物品槽内容。
     *
     * @param input 数据输入
     */
    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        CompoundTag customData = input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());

        virtualSlotResource = ItemResource.EMPTY;
        virtualSlotActive = false;
        virtualSlotAmount = Long.MAX_VALUE;
        virtualSlotAccumulator = 0;
        Arrays.fill(items, ItemStack.EMPTY);

        virtualSlotActive = customData.getBoolean("virtualSlotActive").orElse(false);

        if (virtualSlotActive && customData.contains("virtualSlotResourceStack")) {
            customData.getCompound("virtualSlotResourceStack")
                    .flatMap(tag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, tag).result())
                    .ifPresent(stack -> {
                        if (!stack.isEmpty()) {
                            virtualSlotResource = ItemResource.of(stack);
                            virtualSlotAmount = Long.MAX_VALUE;
                        }
                    });
        }

        tickCounter = customData.getInt("tickCounter").orElse(0);
        if (customData.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(customData.getInt("tier").orElse(0));
        }
        if (customData.contains("energyStored")) {
            this.energyStored = customData.getInt("energyStored").orElse(0);
        }

        updateEnergyStats();
        updateOutputSlots();

        CompoundTag itemsTag = customData.getCompound("items").orElse(new CompoundTag());

        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            int finalI = i;

            itemsTag.getCompound(key).ifPresent(itemTag ->
                    ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag)
                            .result().ifPresent(stack -> items[finalI] = stack)
            );
        }
    }

    /**
     * 获取更新标签，用于客户端同步。
     * <p>
     * 包含 tickCounter、tier、能量、虚拟槽状态以及所有物品槽内容。
     *
     * @param registries 注册表提供者
     * @return 更新用 CompoundTag
     */
    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag outerTag = new CompoundTag();
        CompoundTag customData = new CompoundTag();

        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putInt("energyStored", energyStored);
        customData.putBoolean("virtualSlotActive", virtualSlotActive);

        if (virtualSlotActive && !virtualSlotResource.isEmpty()) {
            ItemStack virtualStack = virtualSlotResource.toStack(1);
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, virtualStack)
                    .result()
                    .ifPresent(tagData -> customData.put("virtualSlotResourceStack", tagData));
        }

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            int finalI = i;
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, items[i])
                    .result()
                    .ifPresent(itemTag -> itemsTag.put("item" + finalI, itemTag));
        }
        customData.put("items", itemsTag);

        outerTag.put("custom_data", customData);
        return outerTag;
    }

    /**
     * 从网络数据包处理更新标签。
     * <p>
     * 清空本地数据后，从数据中恢复 tickCounter、tier、能量、
     * 虚拟槽状态以及所有物品槽内容。
     *
     * @param tag 包含更新数据的 CompoundTag
     */
    public void handleUpdateTagFromPacket(CompoundTag tag) {
        virtualSlotResource = ItemResource.EMPTY;
        virtualSlotActive = false;
        virtualSlotAmount = Long.MAX_VALUE;
        virtualSlotAccumulator = 0;
        Arrays.fill(items, ItemStack.EMPTY);

        CompoundTag data = tag.contains("custom_data")
                ? tag.getCompound("custom_data").orElse(tag)
                : tag;

        tickCounter = data.getInt("tickCounter").orElse(0);
        if (data.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(data.getInt("tier").orElse(0));
        }
        if (data.contains("energyStored")) {
            this.energyStored = data.getInt("energyStored").orElse(0);
        }

        virtualSlotActive = data.getBoolean("virtualSlotActive").orElse(false);
        if (virtualSlotActive && data.contains("virtualSlotResourceStack")) {
            data.getCompound("virtualSlotResourceStack")
                    .flatMap(tagData -> ItemStack.CODEC.parse(NbtOps.INSTANCE, tagData).result())
                    .ifPresent(stack -> {
                        if (!stack.isEmpty()) {
                            virtualSlotResource = ItemResource.of(stack);
                            virtualSlotAmount = Long.MAX_VALUE;
                        }
                    });
        }

        updateEnergyStats();
        updateOutputSlots();

        CompoundTag itemsTag = data.getCompound("items").orElse(new CompoundTag());
        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            int finalI = i;

            if (itemsTag.contains(key)) {
                itemsTag.getCompound(key).flatMap(itemTag ->
                        ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag).result()
                ).ifPresent(stack -> items[finalI] = stack);
            }
        }
    }

    /**
     * 标记方块实体已更新。
     * <p>
     * 调用 setChanged，向所在区块的玩家发送物品复制机同步数据包，
     * 并向周围客户端发送方块更新。
     */
    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            CompoundTag tag = getUpdateTag(level.registryAccess());

            var packet = new ItemReplicatorSyncPacket(getBlockPos(), tag);

            ((ServerLevel) level).getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos().getX() >> 4, getBlockPos().getZ() >> 4), false)
                    .forEach(player -> PacketDistributor.sendToPlayer(player, packet));

            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * 设置 tier 并刷新能量、输出槽与生产速率配置。
     *
     * @param tierId tier 编号
     */
    public void setTier(int tierId) {
        this.tier = ItemReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputSlots();
        updateProductionRate();
    }

    /**
     * 获取当前输出槽数量。
     *
     * @return 输出槽数量
     */
    public int getCurrentOutputSlots() {
        return currentOutputSlots;
    }

    /**
     * 服务端 tick 逻辑。
     * <p>
     * 根据处理速度、能量与输出配置执行物品复制，
     * 优先自动输出到相邻容器，剩余部分写入输出槽。
     * 同时管理虚拟槽的激活与累加器逻辑。
     *
     * @param level       世界
     * @param pos         方块位置
     * @param state       方块状态
     * @param blockEntity 方块实体
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemReplicatorBlockEntity blockEntity) {
        boolean hasItemChanged = false;

        blockEntity.tickCounter++;

        ItemStack inputStack = blockEntity.items[INPUT_SLOT];

        boolean hasRealItems = false;
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + blockEntity.currentOutputSlots; i++) {
            if (!blockEntity.items[i].isEmpty()) {
                hasRealItems = true;
                break;
            }
        }

        if (hasRealItems) {
            if (blockEntity.virtualSlotActive) {
                blockEntity.virtualSlotActive = false;
                blockEntity.virtualSlotResource = ItemResource.EMPTY;
                blockEntity.virtualSlotAmount = Long.MAX_VALUE;
                blockEntity.virtualSlotAccumulator = 0;
                hasItemChanged = true;
            }
        } else {
            if (!inputStack.isEmpty()) {
                int itemsPerProduction = blockEntity.tier.getOutputAmount();
                int ticksPerProduction = blockEntity.tier.getProcessSpeed();

                blockEntity.virtualSlotAccumulator += itemsPerProduction;

                while (blockEntity.virtualSlotAccumulator >= ticksPerProduction) {
                    blockEntity.virtualSlotAccumulator -= ticksPerProduction;

                    if (!blockEntity.virtualSlotActive) {
                        blockEntity.virtualSlotActive = true;
                        blockEntity.virtualSlotResource = ItemResource.of(inputStack);
                        blockEntity.virtualSlotAmount = Long.MAX_VALUE;
                        hasItemChanged = true;
                    }

                    blockEntity.virtualSlotAmount = Long.MAX_VALUE;
                }
            } else {
                if (blockEntity.virtualSlotActive) {
                    blockEntity.virtualSlotActive = false;
                    blockEntity.virtualSlotResource = ItemResource.EMPTY;
                    blockEntity.virtualSlotAmount = Long.MAX_VALUE;
                    blockEntity.virtualSlotAccumulator = 0;
                    hasItemChanged = true;
                }
            }
        }

        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            if (hasItemChanged) {
                blockEntity.markUpdated();
            }
            return;
        }

        blockEntity.tickCounter = 0;

        if (inputStack.isEmpty()) {
            return;
        }

        int itemsToOutput = blockEntity.tier.getOutputAmount();
        int energyPerItem = blockEntity.energyConsumption;

        if (blockEntity.energyStored < energyPerItem) {
            return;
        }

        int remainingToOutput = itemsToOutput;
        boolean hasProduced = false;
        int totalOutput = 0;

        boolean autoOutputEnabled = RRServerConfig.ITEM_REPLICATOR_AUTO_OUTPUT.get();
        if (autoOutputEnabled) {
            Direction outputDirection = RRServerConfig.ITEM_REPLICATOR_AUTO_OUTPUT_DIRECTION.get();
            BlockPos neighborPos = pos.relative(outputDirection);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (!(neighborState.getBlock() instanceof ItemReplicatorBlock)) {
                ResourceHandler<@NotNull ItemResource> handler = level.getCapability(
                        Capabilities.Item.BLOCK,
                        neighborPos,
                        neighborState,
                        level.getBlockEntity(neighborPos),
                        outputDirection.getOpposite()
                );

                if (handler != null) {
                    // 计算可以输出的数量
                    int maxOutputByEnergy = blockEntity.energyStored / energyPerItem;
                    int canOutput = Math.min((int) remainingToOutput, maxOutputByEnergy);

                    if (canOutput > 0) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            ItemResource resource = ItemResource.of(inputStack);
                            long inserted = handler.insert(resource, canOutput, transaction);

                            if (inserted > 0) {
                                transaction.commit();
                                totalOutput += (int) inserted;
                                remainingToOutput -= (int) inserted;
                                hasProduced = true;
                            }
                        }
                    }
                }
            }
        }

        if (remainingToOutput > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + blockEntity.currentOutputSlots && remainingToOutput > 0; i++) {
                    ItemResource resource = ItemResource.of(inputStack);
                    int inserted = blockEntity.itemHandler.insert(i, resource, remainingToOutput, transaction);

                    if (inserted > 0) {
                        remainingToOutput -= inserted;
                        totalOutput += inserted;
                        hasProduced = true;
                    }
                }

                if (totalOutput > 0) {
                    transaction.commit();
                }
            }
        }

        if (hasProduced && totalOutput > 0) {
            int actualEnergyNeeded = totalOutput * energyPerItem;

            if (blockEntity.energyStored >= actualEnergyNeeded) {
                blockEntity.energyStored -= actualEnergyNeeded;
            }
        }

        blockEntity.markUpdated();
    }

    /**
     * 向输入槽添加物品。
     * <p>
     * 仅接受通过 ReplicatorFilter 检查的物品，
     * 输入槽为空时写入单个物品，已有相同物品时增加数量。
     *
     * @param stack 待添加的物品
     * @return 是否添加成功
     */
    public boolean addItem(ItemStack stack) {
        if (!ReplicatorFilter.canInsertItem(stack)) {
            return false;
        }

        if (items[INPUT_SLOT].isEmpty()) {
            ItemStack singleItem = stack.copyWithCount(1);
            items[INPUT_SLOT] = singleItem;

            markUpdated();
            return true;
        } else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
            int canAdd = items[INPUT_SLOT].getMaxStackSize() - items[INPUT_SLOT].getCount();
            if (canAdd > 0 && stack.getCount() > 0) {
                items[INPUT_SLOT].grow(1);

                markUpdated();
                return true;
            }
        }

        return false;
    }

    /**
     * 从输入槽取出物品。
     *
     * @return 取出的物品，空则为 ItemStack.EMPTY
     */
    public ItemStack extractItem() {
        if (!items[INPUT_SLOT].isEmpty()) {
            ItemStack extracted = items[INPUT_SLOT].copy();

            items[INPUT_SLOT] = ItemStack.EMPTY;

            markUpdated();

            return extracted;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 获取物品处理器。
     *
     * @param side 方向，当前忽略
     * @return 物品处理器
     */
    public ResourceHandler<@NotNull ItemResource> getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    /**
     * 获取当前显示在输入槽的物品。
     *
     * @return 输入槽物品
     */
    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
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
     * 能量处理器，允许输入与抽出。
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
            int canReceive = Math.min(maxReceive, energyCapacity - energyStored);
            if (canReceive <= 0) {
                return 0;
            }
            energyStored += canReceive;
            markUpdated();
            return canReceive;
        }

        /**
         * 从内部能量存储抽出能量。
         *
         * @param maxExtract 最大可抽出量
         * @param transaction 事务上下文
         * @return 实际抽出量
         */
        @Override
        public int extract(int maxExtract, @NotNull TransactionContext transaction) {
            int canExtract = Math.min(maxExtract, energyStored);
            if (canExtract <= 0) {
                return 0;
            }
            energyStored -= canExtract;
            markUpdated();
            return canExtract;
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
}