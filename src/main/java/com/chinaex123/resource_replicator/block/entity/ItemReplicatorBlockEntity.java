package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.ItemReplicatorBlock;
import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.network.ItemReplicatorSyncPacket;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ItemReplicatorBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemReplicatorBlockEntity.class);

    private static final int INPUT_SLOT = 0; // 定义输入槽的索引（第 0 个槽位）
    private static final int OUTPUT_SLOT_START = 1; // 定义输出槽的起始索引（第 1 个槽位）
    private static final int MAX_OUTPUT_SLOTS = 9; // 定义最大输出槽数量（9 个输出槽）
    private static final int TOTAL_SLOTS = 1 + MAX_OUTPUT_SLOTS + 1; // 定义总槽位数量（1 个输入槽 + 9 个输出槽 + 1 个虚拟槽位）
    private static final int VIRTUAL_SLOT = 10; // 定义虚拟槽位的索引（第 10 个槽位）

    // 定义所有槽位的物品数组
    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];

    private int tickCounter = 0; // Tick 计数器，用于控制生产速度
    private ItemReplicatorTier tier = ItemReplicatorTier.ITEM_TIER_1; // 机器等级，默认为 1 级
    private int energyStored; // 当前存储的能量
    private int energyCapacity; // 最大能量容量
    private int energyConsumption; // 每次生产的能量消耗
    private int currentOutputSlots; // 当前等级的输出槽数量

    private boolean virtualSlotActive = false;
    private ItemResource virtualSlotResource = ItemResource.EMPTY;
    private int virtualSlotAmount = 0;
    private int virtualSlotAccumulator = 0;

    // 初始化代码块，在构造函数执行前运行
    {
        updateEnergyStats();  // 更新能量统计信息
        energyStored = 0;  // 初始化能量为 0
        updateOutputSlots();  // 更新输出槽数量
        items[VIRTUAL_SLOT] = ItemStack.EMPTY;  // 初始化虚拟槽位为空
    }

    /**
     * 更新能量统计信息（根据机器等级设置容量和消耗）
     */
    private void updateEnergyStats() {
        switch (tier) {
            case ITEM_TIER_1:
                energyCapacity = ServerConfig.getItemTier1EnergyCapacity();
                energyConsumption = ServerConfig.getItemTier1EnergyConsumption();
                break;
            case ITEM_TIER_2:
                energyCapacity = ServerConfig.getItemTier2EnergyCapacity();
                energyConsumption = ServerConfig.getItemTier2EnergyConsumption();
                break;
            case ITEM_TIER_3:
                energyCapacity = ServerConfig.getItemTier3EnergyCapacity();
                energyConsumption = ServerConfig.getItemTier3EnergyConsumption();
                break;
            case ITEM_TIER_4:
                energyCapacity = ServerConfig.getItemTier4EnergyCapacity();
                energyConsumption = ServerConfig.getItemTier4EnergyConsumption();
                break;
            case ITEM_TIER_5:
                energyCapacity = ServerConfig.getItemTier5EnergyCapacity();
                energyConsumption = ServerConfig.getItemTier5EnergyConsumption();
                break;
        }
    }

    /**
     * 更新输出槽数量（根据机器等级）
     */
    private void updateOutputSlots() {
        this.currentOutputSlots = switch (tier) {
            case ITEM_TIER_1 -> ServerConfig.getItemTier1OutputSlots();
            case ITEM_TIER_2 -> ServerConfig.getItemTier2OutputSlots();
            case ITEM_TIER_3 -> ServerConfig.getItemTier3OutputSlots();
            case ITEM_TIER_4 -> ServerConfig.getItemTier4OutputSlots();
            case ITEM_TIER_5 -> ServerConfig.getItemTier5OutputSlots();
        };
    }

    /**
     * 创建物品处理器（实现 ResourceHandler 接口）
     * 泛型参数添加@NotNull 表示 ItemResource 不能为 null
     */
    private final ResourceHandler<@NotNull ItemResource> itemHandler = new ResourceHandler<>() {
        /**
         * 获取槽位数量
         */
        @Override
        public int size() {
            return TOTAL_SLOTS;
        }

        /**
         * 获取指定槽位的资源
         */
        @Override
        public ItemResource getResource(int index) {
            // 如果是虚拟槽位且已激活，返回虚拟资源
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                return virtualSlotResource;
            }
            // 检查索引范围
            if (index >= 0 && index < TOTAL_SLOTS) {
                ItemStack stack = items[index];
                if (!stack.isEmpty()) {
                    return ItemResource.of(stack);
                }
            }
            return ItemResource.EMPTY;
        }

        /**
         * 获取指定槽位的物品数量
         */
        @Override
        public long getAmountAsLong(int index) {
            // 虚拟槽位的数量
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                return virtualSlotAmount;
            }
            // 真实槽位的数量
            if (index >= 0 && index < TOTAL_SLOTS) {
                ItemStack stack = items[index];
                return stack.isEmpty() ? 0L : stack.getCount();
            }
            return 0L;
        }

        /**
         * 获取指定槽位的容量
         */
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            // 虚拟槽位的容量等于一次生产的数量
            if (index == VIRTUAL_SLOT) {
                return tier.getOutputAmount();
            }
            // 其他槽位容量为 64
            return 64L;
        }

        /**
         * 向指定槽位插入物品
         */
        @Override
        public int insert(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
            // 虚拟槽位不允许插入
            if (index == VIRTUAL_SLOT) {
                return 0;
            }

            // 输入槽的处理
            if (index == INPUT_SLOT) {
                if (resource.isEmpty()) {
                    return 0;
                }

                // 检测是否是管道插入
                boolean isPipeInsertion = !isPlayerInsertion();

                // 管道插入时，根据配置决定是否销毁
                if (isPipeInsertion) {
                    if (ServerConfig.isItemReplicatorDestroyEnabled()) {
                        return amount;  // 开启销毁模式：接受并销毁
                    } else {
                        return 0;  // 关闭销毁模式：拒绝插入
                    }
                }

                // 玩家插入：正常处理
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
            // 输出槽完全禁止外部插入
            return 0;
        }

        /**
         * 从指定槽位抽取物品
         */
        @Override
        public int extract(int index, ItemResource resource, int amount, @NotNull TransactionContext transaction) {
            // 虚拟槽位的处理
            if (index == VIRTUAL_SLOT && virtualSlotActive) {
                if (!resource.equals(virtualSlotResource)) {
                    return 0;
                }

                int toExtract = Math.min(amount, virtualSlotAmount);

                if (toExtract > 0) {
                    virtualSlotAmount -= toExtract;
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
         * 检查资源是否可以放入指定槽位
         */
        @Override
        public boolean isValid(int index, ItemResource resource) {
            // 输入槽需要检查过滤器
            if (index == INPUT_SLOT) {
                ItemStack stack = resource.toStack(1);
                return ReplicatorFilter.canInsertItem(stack);
            }
            // 其他槽位允许任何资源
            return true;
        }
    };

    /**
     * 判断是否是玩家插入（通过检查调用栈）
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
     * 构造函数
     */
    public ItemReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_REPLICATOR.get(), pos, state);
        Arrays.fill(items, ItemStack.EMPTY);
    }

    /**
     * 保存额外数据到 NBT（用于存档）
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
            customData.putString("virtualSlotResource", virtualSlotResource.toStack(1).getItem().toString());
        }

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int finalI = i;
                ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, items[i]).result().ifPresent(tag -> itemsTag.put("item" + finalI, tag));
            }
        }
        customData.put("items", itemsTag);

        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    /**
     * 从 NBT 加载额外数据
     */
    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);

        CompoundTag customData = input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());
        virtualSlotActive = customData.getBoolean("virtualSlotActive").orElse(false);
        if (virtualSlotActive && customData.contains("virtualSlotResource")) {
            String resourceName = customData.getString("virtualSlotResource").orElse("");
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

            if (itemsTag.contains(key)) {
                var itemTagOpt = itemsTag.getCompound(key);
                itemTagOpt.flatMap(itemTag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag)
                        .result()).ifPresent(stack -> items[finalI] = stack);
            }
        }

    }

    /**
     * 处理更新标签（不使用）
     */
    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
    }

    /**
     * 获取更新标签用于客户端同步
     */
    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            int finalI = i;
            ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, items[i]).result().ifPresent(itemTag -> itemsTag.put("item" + finalI, itemTag));
        }
        tag.put("items", itemsTag);

        setChanged();

        return tag;
    }

    /**
     * 从网络数据包中更新物品数据
     */
    public void handleUpdateTagFromPacket(CompoundTag tag) {
        tickCounter = tag.getInt("tickCounter").orElse(0);
        if (tag.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(tag.getInt("tier").orElse(0));
        }
        if (tag.contains("energyStored")) {
            this.energyStored = tag.getInt("energyStored").orElse(0);
        }

        updateEnergyStats();
        updateOutputSlots();

        CompoundTag itemsTag = tag.getCompound("items").orElse(new CompoundTag());
        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            int finalI = i;

            if (itemsTag.contains(key)) {
                var itemTagOpt = itemsTag.getCompound(key);
                itemTagOpt.flatMap(itemTag -> ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag)
                        .result()).ifPresent(stack -> items[finalI] = stack);
            } else {
                items[finalI] = ItemStack.EMPTY;
            }
        }
    }

    /**
     * 标记方块为已更新并发送同步包
     */
    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            CompoundTag tag = getUpdateTag(level.registryAccess());

            var packet = new ItemReplicatorSyncPacket(getBlockPos(), tag);

            ((ServerLevel) level).getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false)
                    .forEach(player -> PacketDistributor.sendToPlayer(player, packet));

            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * 设置物品复制机的机器等级
     */
    public void setTier(int tierId) {
        this.tier = ItemReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputSlots();
    }

    /**
     * 获取当前等级的输出槽数量
     */
    public int getCurrentOutputSlots() {
        return currentOutputSlots;
    }

    /**
     * 服务器端的方块实体更新逻辑
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemReplicatorBlockEntity blockEntity) {
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
                blockEntity.virtualSlotAmount = 0;
                blockEntity.virtualSlotAccumulator = 0;
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
                        blockEntity.virtualSlotAmount = 0;
                    }

                    blockEntity.virtualSlotAmount += itemsPerProduction;
                }
            } else {
                blockEntity.virtualSlotActive = false;
                blockEntity.virtualSlotResource = ItemResource.EMPTY;
                blockEntity.virtualSlotAmount = 0;
                blockEntity.virtualSlotAccumulator = 0;
            }
        }

        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
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

        boolean autoOutputEnabled = ServerConfig.isItemReplicatorAutoOutputEnabled();
        if (autoOutputEnabled) {
            Direction outputDirection = ServerConfig.getItemReplicatorAutoOutputDirection();
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
                    try (Transaction transaction = Transaction.openRoot()) {
                        ItemResource resource = ItemResource.of(inputStack);
                        long inserted = handler.insert(resource, remainingToOutput, transaction);

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
                blockEntity.markUpdated();
            }
        }
    }

    /**
     * 向物品复制机添加物品
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
        }
        else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
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
     * 从物品复制机抽取物品
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
     * 获取物品处理器实例
     */
    public ResourceHandler<@NotNull ItemResource> getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    /**
     * 获取用于渲染的显示物品
     */
    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    /**
     * 获取能量处理器
     */
    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    /**
     * 能量处理器实例
     */
    private final EnergyHandler energyHandler = new EnergyHandler() {
        /**
         * 插入能量
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
         * 提取能量（不支持）
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
         * 获取当前能量
         */
        @Override
        public long getAmountAsLong() {
            return energyStored;
        }

        /**
         * 获取最大能量容量
         */

        @Override
        public long getCapacityAsLong() {
            return energyCapacity;
        }
    };
}