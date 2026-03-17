package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ItemReplicatorBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemReplicatorBlockEntity.class);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT_START = 1;
    private static final int MAX_OUTPUT_SLOTS = 9;
    private static final int TOTAL_SLOTS = 1 + MAX_OUTPUT_SLOTS;

    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];
    private int tickCounter = 0;
    private ItemReplicatorTier tier = ItemReplicatorTier.ITEM_TIER_1;
    private int energyStored;
    private int energyCapacity;
    private int energyConsumption;
    private int currentOutputSlots;

    {
        updateEnergyStats();
        energyStored = 0;
        updateOutputSlots();
    }

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
     * 使用 ItemStackResourceHandler 包装器 - 这是 NeoForge 推荐的方式
     */
    private final ItemStackResourceHandler itemHandler = new ItemStackResourceHandler() {
        @Override
        public ItemStack getStack() {
            // 返回整个处理器中所有输出槽物品的聚合视图
            int totalCount = 0;
            ItemStack firstStack = ItemStack.EMPTY;
            
            for (int i = OUTPUT_SLOT_START; i < TOTAL_SLOTS; i++) {
                if (!items[i].isEmpty()) {
                    if (firstStack.isEmpty()) {
                        firstStack = items[i];
                    }
                    if (ItemStack.isSameItemSameComponents(firstStack, items[i])) {
                        totalCount += items[i].getCount();
                    }
                }
            }
            
            if (totalCount == 0) {
                return ItemStack.EMPTY;
            }
            
            return firstStack.copyWithCount(totalCount);
        }

        @Override
        protected void setStack(ItemStack itemStack) {
            // 将所有输出槽设置为指定的物品堆
            for (int i = OUTPUT_SLOT_START; i < TOTAL_SLOTS; i++) {
                if (itemStack.isEmpty()) {
                    items[i] = ItemStack.EMPTY;
                } else {
                    int stackSize = Math.min(itemStack.getMaxStackSize(), itemStack.getCount());
                    items[i] = itemStack.copyWithCount(stackSize);
                }
            }
        }

        public ItemStack getStackInSlot(int slot) {
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                return items[slot];
            }
            return ItemStack.EMPTY;
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            // 检查是否是通过管道（非玩家操作）插入的物品
            boolean isPipeInsertion = !isPlayerInsertion();
            
            // 如果是管道插入，根据配置决定行为
            if (isPipeInsertion) {
                return ServerConfig.isItemReplicatorDestroyEnabled();
            }
            
            // 玩家操作：正常验证
            ItemStack stack = resource.toStack(1);
            return ReplicatorFilter.canInsertItem(stack);
        }
        
        @Override
        protected int getCapacity(ItemResource resource) {
            // 检查是否是通过管道（非玩家操作）插入的物品
            boolean isPipeInsertion = !isPlayerInsertion();
            
            // 如果是管道插入且启用了销毁功能，返回一个很大的值表示"可以全部接受"
            if (isPipeInsertion && ServerConfig.isItemReplicatorDestroyEnabled()) {
                return 999999; // 假装可以接受任意数量的物品
            }
            
            // 正常情况：返回最大堆叠数
            return Math.min(resource.getMaxStackSize(), 99);
        }

        public int insert(int slot, ItemStack stack, int amount, TransactionContext transaction) {
            if (slot != INPUT_SLOT || stack.isEmpty()) {
                return 0;
            }

            // 检查是否是通过管道（非玩家操作）插入的物品
            boolean isPipeInsertion = !isPlayerInsertion();
            
            // 如果是管道插入
            if (isPipeInsertion) {
                if (ServerConfig.isItemReplicatorDestroyEnabled()) {
                    // 启用销毁功能：瞬间销毁，返回 maxAmount 表示全部"消耗"掉了
                    return amount;
                } else {
                    // 未启用销毁功能：拒绝输入，返回 0 表示不接受任何物品
                    return 0;
                }
            }

            // 玩家操作：正常插入逻辑
            ItemStack current = items[slot];

            if (current.isEmpty()) {
                items[slot] = stack.copyWithCount(Math.min(amount, stack.getMaxStackSize()));
                markUpdated();
                return Math.min(amount, stack.getMaxStackSize());
            } else if (ItemStack.isSameItemSameComponents(current, stack)) {
                int canAdd = Math.min(stack.getMaxStackSize() - current.getCount(), amount);
                if (canAdd > 0) {
                    items[slot] = current.copyWithCount(current.getCount() + canAdd);
                    markUpdated();
                }
                return canAdd;
            }

            return 0;
        }

        public int extract(int slot, ItemStack stack, int amount, TransactionContext transaction) {
            if (slot == INPUT_SLOT) {
                return 0;
            }

            if (slot >= OUTPUT_SLOT_START && slot < TOTAL_SLOTS) {
                ItemStack current = items[slot];
                
                if (current.isEmpty()) {
                    return 0;
                }

                if (!ItemStack.isSameItemSameComponents(current, stack)) {
                    return 0;
                }

                int toExtract = Math.min(amount, current.getCount());
                
                if (toExtract <= 0) {
                    return 0;
                }
                
                // 创建剩余物品的副本并立即更新槽位
                ItemStack remaining = current.copyWithCount(current.getCount() - toExtract);
                items[slot] = remaining.isEmpty() ? ItemStack.EMPTY : remaining;
                
                markUpdated();

                return toExtract;
            }

            return 0;
        }

        public long getAmountAsLong(int slot) {
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                ItemStack stack = items[slot];
                return stack.isEmpty() ? 0L : (long) stack.getCount();
            }
            return 0L;
        }

        public long getCapacityAsLong(int slot, ItemStack itemStack) {
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                return 64L;
            }
            return 0L;
        }
    };

    // 判断是否是玩家插入（通过检查调用栈）
    private boolean isPlayerInsertion() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        // 检查调用栈中是否包含 ItemReplicatorBlock 的 useWithoutItem 方法
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("ItemReplicatorBlock") &&
                    element.getMethodName().contains("useWithoutItem")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 物品复制机方块实体构造函数
     * <p>
     * 初始化物品复制机的方块实体，设置方块实体类型并初始化所有槽位为空。
     * </p>
     *
     * @param pos 方块在世界中的坐标位置
     * @param state 方块的当前状态
     */
    public ItemReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_REPLICATOR.get(), pos, state);
        // 初始化所有槽位为空
        Arrays.fill(items, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);

        CompoundTag customData = new CompoundTag();
        customData.putInt("tickCounter", tickCounter);
        customData.putInt("tier", tier.getId());
        customData.putInt("energyStored", energyStored);

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int finalI = i;
                ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, items[i]).result().ifPresent(tag -> itemsTag.put("item" + finalI, tag));
            }
        }
        customData.put("items", itemsTag);

        output.store("custom_data", CompoundTag.CODEC, customData);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        LOGGER.info("=== loadAdditional 被调用 ===");
        
        if (level != null) {
            LOGGER.info("Level side: {}", level.isClientSide() ? "CLIENT" : "SERVER");
        }

        CompoundTag customData = (CompoundTag) input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());
        LOGGER.info("customData: {}", customData);

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
        LOGGER.info("itemsTag: {}", itemsTag);
        
        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            int finalI = i;
            
            // 使用 contains 方法检查标签是否存在（和流体复制机一致）
            if (itemsTag.contains(key)) {
                var itemTagOpt = itemsTag.getCompound(key);
                itemTagOpt.ifPresent(itemTag -> {
                    ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> {
                        items[finalI] = stack;
                        LOGGER.info("加载物品到槽位 {}: {}", finalI, stack.getItem());
                    });
                });
            } else {
                LOGGER.info("槽位 {} 没有 NBT 数据", i);
            }
        }
        
        LOGGER.info("=== loadAdditional 结束 ===");
    }

    @Override
    public void handleUpdateTag(net.minecraft.world.level.storage.ValueInput input) {
        // 不再使用这个方法，改用自定义网络包
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        LOGGER.info("getUpdateTag 被调用，registries: {}", registries);
        
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            int finalI = i;
            // 即使槽位为空也要打包，确保客户端知道槽位是空的
            ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, items[i]).result().ifPresent(itemTag -> {
                itemsTag.put("item" + finalI, itemTag);
                LOGGER.info("打包物品到 NBT: 槽位 {}, 物品 {}", finalI, items[finalI].getItem());
            });
        }
        tag.put("items", itemsTag);
        
        LOGGER.info("getUpdateTag 返回：{}", tag);

        // 关键修改：同时调用 setChanged() 来强制保存和同步
        setChanged();

        return tag;
    }

    /**
     * 从网络数据包中更新物品数据
     */
    public void handleUpdateTagFromPacket(net.minecraft.nbt.CompoundTag tag) {
        LOGGER.info("收到网络数据包更新");
        
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
            
            // 使用 contains 方法检查标签是否存在（和 loadAdditional 一致）
            if (itemsTag.contains(key)) {
                var itemTagOpt = itemsTag.getCompound(key);
                itemTagOpt.ifPresent(itemTag -> {
                    ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> {
                        items[finalI] = stack;
                        LOGGER.info("从网络加载物品到槽位 {}: {}", finalI, stack.getItem());
                    });
                });
            } else {
                // 关键修改：如果标签不存在，设置为空物品
                items[finalI] = ItemStack.EMPTY;
                LOGGER.info("槽位 {} 没有 NBT 数据，设置为空", i);
            }
        }
    }

    private void markUpdated() {
        LOGGER.info("markUpdated 被调用");
        setChanged();
        if (level != null && !level.isClientSide()) {
            CompoundTag tag = getUpdateTag(level.registryAccess());
            
            // 发送自定义网络包到客户端
            var packet = new com.chinaex123.resource_replicator.network.ItemReplicatorSyncPacket(getBlockPos(), tag);
            
            // 发送给所有追踪这个方块的玩家
            ((net.minecraft.server.level.ServerLevel) level).getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(getBlockPos()), false)
                .forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet));
            
            LOGGER.info("已发送自定义数据包到客户端，位置={}", getBlockPos());
            
            // 关键修改：强制通知客户端方块实体已更新，需要重新渲染
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    /**
     * 设置物品复制机的机器等级
     * <p>
     * 此方法用于设置物品复制机的机器等级，通常在方块被放置或升级时调用。
     * </p>
     *
     * @param tierId 机器等级的 ID
     */
    public void setTier(int tierId) {
        this.tier = ItemReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputSlots();
    }

    /**
     * 获取当前等级的输出槽数量
     *
     * @return int 当前等级的输出槽数量
     */
    public int getCurrentOutputSlots() {
        return currentOutputSlots;
    }

    /**
     * 服务器端的方块实体更新逻辑
     * <p>
     * 此方法在服务器端每 tick 调用一次，用于处理物品复制机的复制逻辑。
     * 它会检查输入槽中的物品，并根据机器等级的复制速度和复制数量进行复制操作。
     * </p>
     *
     * @param level 当前方块所在的级别
     * @param pos 方块在世界中的坐标位置
     * @param state 方块的当前状态
     * @param blockEntity 当前方块实体
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemReplicatorBlockEntity blockEntity) {
        // 累加刻计数器
        blockEntity.tickCounter++;

        // 当刻计数器未达到处理速度时，不执行逻辑
        if (blockEntity.tickCounter < blockEntity.tier.getProcessSpeed()) {
            return;
        }

        // 1. 基础检查：输入槽是否有样板物品
        ItemStack inputStack = blockEntity.items[INPUT_SLOT];
        if (inputStack.isEmpty()) {
            blockEntity.tickCounter = 0;
            return;
        }

        // 2. 初始化参数
        int targetOutput = blockEntity.tier.getOutputAmount();
        int energyPerItem = blockEntity.energyConsumption;
        int remainingOutput = targetOutput;
        int totalActuallyProduced = 0;

        // 3. 能量预检查：如果连 1 个物品的能量都不够，直接跳过
        if (blockEntity.energyStored < energyPerItem) {
            return;
        }

        // ========== 第一步：尝试自动输出到相邻容器 ==========
        if (ServerConfig.isItemReplicatorAutoOutputEnabled()) {
            Direction outputDirection = ServerConfig.getItemReplicatorAutoOutputDirection();
            BlockPos neighborPos = pos.relative(outputDirection);

            var neighborHandler = level.getCapability(Capabilities.Item.BLOCK, neighborPos, outputDirection.getOpposite());

            if (neighborHandler != null) {
                try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                    net.neoforged.neoforge.transfer.item.ItemResource resource = net.neoforged.neoforge.transfer.item.ItemResource.of(inputStack);

                    int maxByEnergy = blockEntity.energyStored / energyPerItem;
                    int amountToTry = Math.min(remainingOutput, maxByEnergy);

                    int inserted = (int) neighborHandler.insert(resource, amountToTry, transaction);

                    if (inserted > 0) {
                        transaction.commit();
                        remainingOutput -= inserted;
                        totalActuallyProduced += inserted;
                    }
                }
            }
        }

        // ========== 第二步：尝试存入机器内部的缓存槽 (OUTPUT SLOTS) ==========
        if (remainingOutput > 0) {
            int maxSlotIndex = Math.min(TOTAL_SLOTS, OUTPUT_SLOT_START + blockEntity.currentOutputSlots);

            for (int i = OUTPUT_SLOT_START; i < maxSlotIndex && remainingOutput > 0; i++) {
                if (blockEntity.energyStored < (totalActuallyProduced + 1) * energyPerItem) {
                    break;
                }

                ItemStack slotStack = blockEntity.items[i];

                if (slotStack.isEmpty()) {
                    int addAmount = Math.min(remainingOutput, inputStack.getMaxStackSize());
                    blockEntity.items[i] = inputStack.copyWithCount(addAmount);

                    remainingOutput -= addAmount;
                    totalActuallyProduced += addAmount;
                }
                else if (ItemStack.isSameItemSameComponents(slotStack, inputStack)) {
                    int canAccept = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (canAccept > 0) {
                        int addAmount = Math.min(remainingOutput, canAccept);
                        slotStack.grow(addAmount);

                        remainingOutput -= addAmount;
                        totalActuallyProduced += addAmount;
                    }
                }
            }
        }

        // ========== 第三步：结算费用与状态更新 ==========
        if (totalActuallyProduced > 0) {
            int totalEnergyCost = totalActuallyProduced * energyPerItem;
            blockEntity.energyStored -= totalEnergyCost;

            blockEntity.tickCounter = 0;
            blockEntity.markUpdated();
        } else {
            if (remainingOutput == targetOutput) {
                blockEntity.tickCounter = 0;
            }
        }
    }

    /**
     * 向物品复制机添加物品
     */
    public boolean addItem(ItemStack stack) {
        LOGGER.info("addItem 被调用，物品：{}, 数量：{}", stack.getItem(), stack.getCount());
        
        if (!ReplicatorFilter.canInsertItem(stack)) {
            LOGGER.info("物品不能放入输入槽");
            return false;
        }

        if (items[INPUT_SLOT].isEmpty()) {
            ItemStack singleItem = stack.copyWithCount(1);
            items[INPUT_SLOT] = singleItem;
            LOGGER.info("放入物品到输入槽：{}", singleItem.getItem());
            
            markUpdated();
            return true;
        }
        else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
            int canAdd = items[INPUT_SLOT].getMaxStackSize() - items[INPUT_SLOT].getCount();
            if (canAdd > 0 && stack.getCount() > 0) {
                items[INPUT_SLOT].grow(1);
                LOGGER.info("增加输入槽物品数量：{}", items[INPUT_SLOT].getCount());
                
                markUpdated();
                return true;
            }
        }
        
        LOGGER.info("添加失败，输入槽已有不同物品或已满");
        return false;
    }

    /**
     * 从物品复制机抽取物品
     *
     * @return ItemStack 输入槽中的物品堆，如果为空则返回 EMPTY
     */
    public ItemStack extractItem() {
        LOGGER.info("=== extractItem 被调用 ===");
        LOGGER.info("提取前 - 输入槽：{}, 数量：{}", items[INPUT_SLOT].getItem(), items[INPUT_SLOT].getCount());
        
        if (!items[INPUT_SLOT].isEmpty()) {
            // 取出整个输入槽的物品，而不是只取 1 个
            ItemStack extracted = items[INPUT_SLOT].copy();
            LOGGER.info("准备提取物品：{}, 数量：{}", extracted.getItem(), extracted.getCount());
            
            // 清空输入槽
            items[INPUT_SLOT] = ItemStack.EMPTY;
            LOGGER.info("输入槽已清空");
            
            // 同时清空所有输出槽/缓存槽
            for (int i = OUTPUT_SLOT_START; i < TOTAL_SLOTS; i++) {
                if (!items[i].isEmpty()) {
                    LOGGER.info("清空输出槽 {}: {}, 数量：{}", i, items[i].getItem(), items[i].getCount());
                    items[i] = ItemStack.EMPTY;
                }
            }
            
            // 标记更新，发送同步包
            markUpdated();
            LOGGER.info("已调用 markUpdated");
            
            return extracted;
        }
        
        LOGGER.info("输入槽为空，返回 EMPTY");
        return ItemStack.EMPTY;
    }

    /**
     * 获取物品处理器实例
     *
     * @param side 方向（可选），如果为 null 则返回通用处理器
     * @return ItemStackResourceHandler 物品处理器实例
     */
    public net.neoforged.neoforge.transfer.item.ItemStackResourceHandler getItemHandler(@Nullable Direction side) {
        // 直接返回 itemHandler，它已经有管道检测逻辑了
        return itemHandler;
    }

    /**
     * 获取用于渲染的显示物品（从输入槽获取）
     */
    public ItemStack getDisplayedItem() {
        // 移除日志，直接返回物品
        return items[INPUT_SLOT];
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

    /**
     * 能量处理器
     */
    private final EnergyHandler energyHandler = new EnergyHandler() {
        @Override
        public int insert(int maxReceive, TransactionContext transaction) {
            int canReceive = Math.min(maxReceive, energyCapacity - energyStored);
            if (canReceive <= 0) {
                return 0;
            }
            energyStored += canReceive;
            markUpdated();
            return canReceive;
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            int canExtract = Math.min(maxExtract, energyStored);
            if (canExtract <= 0) {
                return 0;
            }
            energyStored -= canExtract;
            markUpdated();
            return canExtract;
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
}
