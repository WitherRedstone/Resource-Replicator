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

        public boolean isValid(int slot, ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return ReplicatorFilter.canInsertItem(stack);
            }
            if (slot >= OUTPUT_SLOT_START && slot < TOTAL_SLOTS) {
                return !items[slot].isEmpty();
            }
            return false;
        }

        public int insert(int slot, ItemStack stack, int amount, TransactionContext transaction) {
            if (slot != INPUT_SLOT || stack.isEmpty()) {
                return 0;
            }

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

        CompoundTag customData = (CompoundTag) input.read("custom_data", CompoundTag.CODEC).orElse(new CompoundTag());

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
            itemsTag.getCompound(key).ifPresent(itemTag -> {
                ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> items[finalI] = stack);
            });
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                int finalI = i;
                ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, items[i]).result().ifPresent(itemTag -> itemsTag.put("item" + finalI, itemTag));
            }
        }
        tag.put("items", itemsTag);

        return tag;
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
            blockEntity.tickCounter = 0; // 如果没东西，重置计数器防止跳过下一次放入
            return;
        }

        // 2. 初始化参数
        int targetOutput = blockEntity.tier.getOutputAmount();   // 本次循环理论产量
        int energyPerItem = blockEntity.energyConsumption;      // 单个物品能耗
        int remainingOutput = targetOutput;                    // 剩余待处理产量
        int totalActuallyProduced = 0;                         // 本次 tick 实际产出的数量

        // 3. 能量预检查：如果连 1 个物品的能量都不够，直接跳过
        if (blockEntity.energyStored < energyPerItem) {
            LOGGER.warn("能量不足，无法开始复制操作");
            return;
        }

        // ========== 第一步：尝试自动输出到相邻容器 ==========
        if (ServerConfig.isItemReplicatorAutoOutputEnabled()) {
            Direction outputDirection = ServerConfig.getItemReplicatorAutoOutputDirection();
            BlockPos neighborPos = pos.relative(outputDirection);

            // 获取相邻方块的物品处理器 (使用 NeoForge Transfer API)
            var neighborHandler = level.getCapability(Capabilities.Item.BLOCK, neighborPos, outputDirection.getOpposite());

            if (neighborHandler != null) {
                // 开启事务处理
                try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                    net.neoforged.neoforge.transfer.item.ItemResource resource = net.neoforged.neoforge.transfer.item.ItemResource.of(inputStack);

                    // 计算基于当前剩余能量，最多还能自动输出多少个
                    int maxByEnergy = blockEntity.energyStored / energyPerItem;
                    int amountToTry = Math.min(remainingOutput, maxByEnergy);

                    // 尝试插入
                    int inserted = (int) neighborHandler.insert(resource, amountToTry, transaction);

                    if (inserted > 0) {
                        // 提交事务：只有在这里，物品才真正进入邻居容器
                        transaction.commit();
                        remainingOutput -= inserted;
                        totalActuallyProduced += inserted;
                        LOGGER.debug("自动输出成功：插入了 {} 个物品", inserted);
                    }
                }
            }
        }

        // ========== 第二步：尝试存入机器内部的缓存槽 (OUTPUT SLOTS) ==========
        if (remainingOutput > 0) {
            // 确定当前等级允许访问的最大槽位索引
            int maxSlotIndex = Math.min(TOTAL_SLOTS, OUTPUT_SLOT_START + blockEntity.currentOutputSlots);
            
            LOGGER.info("=== 开始存入缓存槽 ===");
            LOGGER.info("剩余待输出：{}, 输出槽数量：{}", remainingOutput, blockEntity.currentOutputSlots);

            for (int i = OUTPUT_SLOT_START; i < maxSlotIndex && remainingOutput > 0; i++) {
                // 每次循环前再次检查能量：确保产出的每一个都有能量支撑
                if (blockEntity.energyStored < (totalActuallyProduced + 1) * energyPerItem) {
                    LOGGER.warn("能量不足，提前退出循环");
                    break;
                }

                ItemStack slotStack = blockEntity.items[i];
                LOGGER.info("检查输出槽 {}: {}, 数量={}", 
                    i, 
                    slotStack.isEmpty() ? "空" : slotStack.getDisplayName().getString(),
                    slotStack.getCount());

                // 情况 A: 槽位为空
                if (slotStack.isEmpty()) {
                    int addAmount = Math.min(remainingOutput, inputStack.getMaxStackSize());
                    blockEntity.items[i] = inputStack.copyWithCount(addAmount);
                    
                    LOGGER.info("✓ 放入输出槽 {}: {} 个 {}", i, addAmount, inputStack.getDisplayName().getString());

                    remainingOutput -= addAmount;
                    totalActuallyProduced += addAmount;
                }
                // 情况 B: 槽位已有相同物品，尝试堆叠
                else if (ItemStack.isSameItemSameComponents(slotStack, inputStack)) {
                    int canAccept = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (canAccept > 0) {
                        int addAmount = Math.min(remainingOutput, canAccept);
                        slotStack.grow(addAmount);
                        
                        LOGGER.info("✓ 堆叠到输出槽 {}: 添加 {} 个，现在总数 {}", i, addAmount, slotStack.getCount());

                        remainingOutput -= addAmount;
                        totalActuallyProduced += addAmount;
                    }
                }
                else {
                    LOGGER.warn("输出槽 {} 的物品类型不匹配：期望={}, 实际={}", 
                        i, inputStack.getDisplayName().getString(), slotStack.getDisplayName().getString());
                }
            }
            
            LOGGER.info("缓存槽存入完成：剩余={}, 实际产出={}", remainingOutput, totalActuallyProduced);
        }

        // ========== 第三步：结算费用与状态更新 ==========
        if (totalActuallyProduced > 0) {
            // 真正产生了物品，才扣除对应的能量
            int totalEnergyCost = totalActuallyProduced * energyPerItem;
            blockEntity.energyStored -= totalEnergyCost;

            // 只有产出了才重置 tick 计时器
            blockEntity.tickCounter = 0;
            blockEntity.markUpdated();

            LOGGER.info("复制循环完成：产出 {} 个，消耗 {} FE", totalActuallyProduced, totalEnergyCost);
        } else {
            // 如果一整个 tick 下来一个物品都没能产出（可能因为输出槽全满且自动输出也满了）
            // 我们将计数器重置为 0，让它进入下一个等待周期，防止在高频 tick 中造成不必要的性能浪费
            if (remainingOutput == targetOutput) {
                blockEntity.tickCounter = 0;
                LOGGER.warn("输出槽及外部容器已满，产出中止");
            }
        }
    }

    /**
     * 向物品复制机添加物品
     *
     * @param stack 要添加的物品堆
     * @return boolean 如果物品成功添加则返回 true，否则返回 false
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
     *
     * @return ItemStack 输入槽中的物品堆，如果为空则返回 EMPTY
     */
    public ItemStack extractItem() {
        if (!items[INPUT_SLOT].isEmpty()) {
            ItemStack extracted = items[INPUT_SLOT].copyWithCount(1);
            items[INPUT_SLOT].shrink(1);
            markUpdated();
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 标记方块实体已更新
     */
    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 获取物品复制机当前显示的物品
     *
     * @return ItemStack 输入槽中的物品堆，如果为空则返回 EMPTY
     */
    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    /**
     * 获取物品处理器实例
     *
     * @param side 方向（可选），如果为 null 则返回通用处理器
     * @return ItemStackResourceHandler 物品处理器实例
     */
    public net.neoforged.neoforge.transfer.item.ItemStackResourceHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
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
