package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
import com.chinaex123.resource_replicator.util.ReplicatorFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ItemReplicatorBlockEntity extends BlockEntity {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT_START = 1;
    private static final int TOTAL_SLOTS;

    static {
        TOTAL_SLOTS = 1 + ServerConfig.getItemReplicatorOutputSlots();
    }

    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];
    private int tickCounter = 0;
    private ItemReplicatorTier tier = ItemReplicatorTier.ITEM_TIER_1;

    /**
     * 物品处理器接口实现
     * <p>
     * 此接口用于处理物品的输入输出操作，实现了 NeoForge 的物品自动化交互。
     * 包含一个输入槽（INPUT_SLOT）和多个输出槽（OUTPUT_SLOT_START 到 TOTAL_SLOTS）。
     * </p>
     */
    private final IItemHandler itemHandler = new IItemHandler() {
        /**
         * 获取可用槽位的总数量
         * 
         * @return int 固定返回 TOTAL_SLOTS（1 个输入槽 + 多个输出槽）
         */
        @Override
        public int getSlots() {
            return TOTAL_SLOTS;
        }

        /**
         * 获取指定槽位中的物品堆
         * 
         * @param slot 槽位索引
         * @return ItemStack 指定槽位中的物品堆，如果索引无效或为空则返回 EMPTY
         */
        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == INPUT_SLOT) {
                return items[INPUT_SLOT];
            } else if (slot >= OUTPUT_SLOT_START && slot < TOTAL_SLOTS) {
                return items[slot];
            }
            return ItemStack.EMPTY;
        }

        /**
         * 向指定槽位插入物品
         * <p>
         * 此方法处理物品的输入逻辑，支持配置控制的管道销毁功能：
         * </p>
         * <ul>
         *     <li><strong>黑名单过滤</strong>：首先检查物品是否在黑名单中，如果是则拒绝</li>
         *     <li><strong>销毁功能开启</strong>：管道输入的物品会被瞬间销毁（返回 EMPTY）</li>
         *     <li><strong>销毁功能关闭</strong>：拒绝管道输入（返回原 stack）</li>
         *     <li><strong>玩家操作</strong>：正常存入输入槽</li>
         * </ul>
         * 
         * @param slot 要插入的槽位索引（只接受 INPUT_SLOT）
         * @param stack 要插入的物品堆
         * @param simulate 是否仅为模拟（true = 不实际修改，false = 实际执行）
         * @return ItemStack 插入后剩余的物品堆，如果全部插入成功则返回 EMPTY
         */
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            // 只处理输入槽
            if (slot != INPUT_SLOT) {
                return stack;
            }

            // 检查是否是通过管道（非玩家操作）插入的物品
            boolean isPipeInsertion = !isPlayerInsertion();

            // 如果是管道插入到输入槽
            if (isPipeInsertion) {
                if (ServerConfig.isItemReplicatorDestroyEnabled()) {
                    // 启用销毁功能：瞬间销毁，返回空表示全部"消耗"掉了
                    return ItemStack.EMPTY;
                } else {
                    // 未启用销毁功能：拒绝输入，返回原 stack
                    return stack;
                }
            }

            // 玩家操作：正常存入输入槽
            if (!simulate) {
                if (items[INPUT_SLOT].isEmpty()) {
                    items[INPUT_SLOT] = stack.copy();
                    markUpdated();
                    return ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
                    int canAdd = stack.getMaxStackSize() - items[INPUT_SLOT].getCount();
                    if (canAdd > 0) {
                        items[INPUT_SLOT].grow(Math.min(canAdd, stack.getCount()));
                        markUpdated();
                        return stack.copyWithCount(Math.max(0, stack.getCount() - canAdd));
                    }
                }
            }

            return stack;
        }

        /**
         * 从指定槽位抽取物品
         * <p>
         * 只能从输出槽抽取物品，输入槽不允许直接抽取。
         * </p>
         * 
         * @param slot 要抽取的槽位索引
         * @param amount 最大可抽取的物品数量
         * @param simulate 是否仅为模拟（true = 不实际修改，false = 实际执行）
         * @return ItemStack 实际抽取到的物品堆，如果无法抽取则返回 EMPTY
         */
        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == INPUT_SLOT) {
                return ItemStack.EMPTY;
            } else if (slot >= OUTPUT_SLOT_START && slot < TOTAL_SLOTS) {
                if (items[slot].isEmpty()) {
                    return ItemStack.EMPTY;
                }

                int extractAmount = Math.min(amount, items[slot].getCount());
                ItemStack result = items[slot].split(extractAmount);

                if (!simulate) {
                    markUpdated();
                }

                return result;
            }
            return ItemStack.EMPTY;
        }

        /**
         * 获取指定槽位的最大堆叠数量限制
         * 
         * @param slot 槽位索引
         * @return int 固定返回 64（所有槽位都遵循物品的 maxStackSize）
         */
        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        /**
         * 检查物品是否可以放入指定槽位
         * <p>
         * 此方法在物品尝试进入槽位时被调用，会委托给 ReplicatorFilter 进行黑白名单检查。
         * 对于管道插入，会根据配置的销毁功能决定是否接受物品。
         * </p>
         * 
         * @param slot 槽位索引
         * @param stack 待检查的物品堆
         * @return boolean 如果该物品可以放入则返回 true，否则返回 false
         */
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            // 输入槽只接受玩家放置的物品，管道需要根据配置判断
            if (slot == INPUT_SLOT) {
                if (isPlayerInsertion()) {
                    return ReplicatorFilter.canInsertItem(stack);
                } else {
                    // 管道插入：如果启用销毁则接受（然后销毁），否则拒绝
                    return ServerConfig.isItemReplicatorDestroyEnabled();
                }
            }
            return false;
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

    /**
     * 保存方块实体的额外数据到 NBT 标签
     * <p>
     * 此方法在方块实体数据需要持久化时被调用（如世界保存、区块卸载等）。
     * 它会将当前 tick 计数器、机器等级以及所有槽位中的物品数据写入 NBT 标签，
     * 确保方块状态在重启世界后能够正确恢复。
     * </p>
     * 
     * @param tag 用于存储数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于序列化物品的注册表信息
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // 保存刻计数器和机器等级
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());

        // 遍历所有槽位并保存非空物品
        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                itemsTag.put("item" + i, items[i].save(registries));
            }
        }
        tag.put("items", itemsTag);
    }

    /**
     * 从 NBT 标签加载方块实体的额外数据
     * <p>
     * 此方法在方块实体数据需要从 NBT 标签恢复时被调用（如世界加载、区块加载等）。
     * 它会从 NBT 标签中读取 tick 计数器、机器等级以及所有槽位中的物品数据，
     * 确保方块状态在重启世界后能够正确恢复。
     * </p>
     * 
     * @param tag 包含数据的 CompoundTag 对象
     * @param registries 注册表查找提供器，用于反序列化物品的注册表信息
     */
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // 读取刻计数器和机器等级
        tickCounter = tag.getInt("tickCounter");
        if (tag.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(tag.getInt("tier"));
        }

        // 遍历所有槽位并加载非空物品
        CompoundTag itemsTag = tag.getCompound("items");
        for (int i = 0; i < items.length; i++) {
            String key = "item" + i;
            if (itemsTag.contains(key)) {
                items[i] = ItemStack.parseOptional(registries, itemsTag.getCompound(key));
            } else {
                items[i] = ItemStack.EMPTY;
            }
        }
    }

    /**
     * 获取用于同步方块实体数据到客户端的网络数据包
     * <p>
     * 此方法在服务器需要向客户端发送方块实体更新信息时被调用。
     * 它会创建一个客户端边界方块实体数据包，将当前的物品状态、刻计数器等信息
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
     * 获取用于同步方块实体数据到客户端的 NBT 标签
     * <p>
     * 此方法在方块实体数据需要同步到客户端时被调用（如玩家靠近方块）。
     * 它会创建一个包含方块实体数据的 NBT 标签，确保客户端能够正确显示方块状态。
     * </p>
     * 
     * @param registries 注册表查找提供器，用于序列化物品的注册表信息
     * @return CompoundTag 包含方块实体数据的 NBT 标签
     */
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                itemsTag.put("item" + i, items[i].save(registries));
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
        blockEntity.tickCounter++;

        // 如果刻计数器达到处理速度，开始复制逻辑
        if (blockEntity.tickCounter >= blockEntity.tier.getProcessSpeed()) {
            // 重置刻计数器
            blockEntity.tickCounter = 0;

            // 获取输入槽中的物品
            ItemStack inputStack = blockEntity.items[INPUT_SLOT];
            // 如果输入槽不为空
            if (!inputStack.isEmpty()) {
                // 获取当前等级的输出数量
                int remainingOutput = blockEntity.tier.getOutputAmount();
                // 标记是否有更新
                boolean hasUpdated = false;

                // 获取六个方向的数组
                Direction[] directions = Direction.values();
                // 遍历六个方向尝试向相邻容器输出物品
                for (Direction dir : directions) {
                    // 如果剩余输出量已为 0，跳出循环
                    if (remainingOutput <= 0) break;

                    // 获取相邻方块的坐标
                    BlockPos neighborPos = pos.relative(dir);
                    // 获取相邻方块的物品处理器（从相反方向访问）
                    IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());

                    // 如果相邻方块有物品处理器
                    if (handler != null) {
                        // 创建要输出的物品堆（每次输出 1 个）
                        ItemStack outputStack = inputStack.copy();
                        outputStack.setCount(1);

                        // 尝试将物品插入到相邻容器的处理器中
                        ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputStack, false);

                        // 如果全部插入成功（remainder 为空）
                        if (remainder.isEmpty()) {
                            // 减少剩余输出量
                            remainingOutput--;
                            // 标记已更新
                            hasUpdated = true;
                        }
                    }
                }

                // 如果还有剩余的输出量，存入输出槽
                if (remainingOutput > 0) {
                    // 遍历所有输出槽
                    for (int slot = OUTPUT_SLOT_START; slot < TOTAL_SLOTS && remainingOutput > 0; slot++) {
                        // 获取当前输出槽的物品
                        ItemStack currentOutput = blockEntity.items[slot];

                        // 如果当前输出槽为空
                        if (currentOutput.isEmpty()) {
                            // 创建新的物品堆，数量为剩余输出量和物品最大堆叠数的较小值
                            ItemStack newOutput = inputStack.copy();
                            newOutput.setCount(Math.min(remainingOutput, inputStack.getMaxStackSize()));
                            // 放入输出槽
                            blockEntity.items[slot] = newOutput;
                            // 减少剩余输出量
                            remainingOutput -= newOutput.getCount();
                            // 标记已更新
                            hasUpdated = true;
                        } 
                        // 如果当前输出槽的物品与输入物品相同
                        else if (ItemStack.isSameItemSameComponents(currentOutput, inputStack)) {
                            // 计算还能添加的数量（取物品最大堆叠数减去当前数量和剩余输出量的较小值）
                            int canAdd = Math.min(
                                    inputStack.getMaxStackSize() - currentOutput.getCount(),
                                    remainingOutput
                            );
                            // 如果可以添加
                            if (canAdd > 0) {
                                // 增加当前输出槽的物品数量
                                currentOutput.grow(canAdd);
                                // 减少剩余输出量
                                remainingOutput -= canAdd;
                                // 标记已更新
                                hasUpdated = true;
                            }
                        }
                    }
                }

                // 如果有更新，标记方块实体并同步到客户端
                if (hasUpdated) {
                    blockEntity.markUpdated();
                }
            }
        }
    }

    /**
     * 向物品复制机添加物品
     * <p>
     * 此方法用于向物品复制机的输入槽添加物品，通常在玩家放置物品时调用。
     * 它会检查物品是否可以放入，并根据物品堆叠规则进行处理。
     * </p>
     * 
     * @param stack 要添加的物品堆
     * @return boolean 如果物品成功添加则返回 true，否则返回 false
     */
    public boolean addItem(ItemStack stack) {
        if (!ReplicatorFilter.canInsertItem(stack)) {
            return false;
        }

        // 如果输入槽为空，直接放入物品
        if (items[INPUT_SLOT].isEmpty()) {
            // 复制物品堆并放入输入槽
            items[INPUT_SLOT] = stack.copy();
            // 标记方块为已更新状态
            markUpdated();
            return true;
        } 
        // 如果输入槽中的物品与要放入的物品相同
        else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
            // 计算还能添加的数量（最大堆叠数减去当前数量）
            int canAdd = stack.getMaxStackSize() - items[INPUT_SLOT].getCount();
            // 如果还可以添加物品
            if (canAdd > 0) {
                // 增加输入槽中的物品数量（取可添加数量和物品总数的较小值）
                items[INPUT_SLOT].grow(Math.min(canAdd, stack.getCount()));
                // 标记方块为已更新状态
                markUpdated();
                return true;
            }
        }
        return false;
    }

    /**
     * 从物品复制机抽取物品
     * <p>
     * 此方法用于从物品复制机的输入槽抽取物品，通常在玩家取出物品时调用。
     * 它会清空输入槽并返回其中的物品堆。
     * </p>
     * 
     * @return ItemStack 输入槽中的物品堆，如果为空则返回 EMPTY
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
     * 标记方块实体已更新
     * <p>
     * 此方法用于标记方块实体已更新，通常在方块实体数据发生变化时调用。
     * 它会通知世界方块实体已更新，并触发方块实体数据的同步。
     * </p>
     */
    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 获取物品复制机当前显示的物品
     * <p>
     * 此方法用于获取物品复制机当前显示的物品，通常用于渲染和显示。
     * 它会返回输入槽中的物品堆。
     * </p>
     * 
     * @return ItemStack 输入槽中的物品堆，如果为空则返回 EMPTY
     */
    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    /**
     * 获取物品处理器接口
     * <p>
     * 此方法用于获取物品处理器接口，通常在物品自动化交互时调用。
     * 它会返回物品处理器接口的实现。
     * </p>
     * 
     * @param side 方块的朝向，如果为 null 则返回默认的物品处理器接口
     * @return IItemHandler 物品处理器接口的实现
     */
    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }
}
