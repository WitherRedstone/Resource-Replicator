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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ItemReplicatorBlockEntity extends BlockEntity {
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
        tag.putInt("energyStored", energyStored);

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
        if (tag.contains("energyStored")) {
            this.energyStored = tag.getInt("energyStored");
        }
        
        // 加载等级后更新能量和输出槽配置
        updateEnergyStats();
        updateOutputSlots();

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
        tag.putInt("energyStored", energyStored);

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
        // 累加刻计数器，用于控制复制速度
        blockEntity.tickCounter++;

        // 当刻计数器达到处理速度时，执行复制操作
        // getProcessSpeed() 返回完成一次复制需要多少刻
        if (blockEntity.tickCounter >= blockEntity.tier.getProcessSpeed()) {
            // 获取输入槽的物品（INPUT_SLOT = 0）
            ItemStack inputStack = blockEntity.items[INPUT_SLOT];

            // 如果输入槽不为空（有物品）
            if (!inputStack.isEmpty()) {
                // 获取目标产出数量（不同等级产量不同）
                int targetOutput = blockEntity.tier.getOutputAmount();
                // 获取每个物品的能耗（从 config 读取）
                int energyPerItem = blockEntity.energyConsumption;

                // ========== 预检查能量 ==========
                // 先检查是否有足够的能量支持至少 1 个物品的输出
                // 如果连 1 个物品的能量都不够，直接返回停止后续处理
                if (blockEntity.energyStored < energyPerItem) {
                    return;
                }

                // 剩余待输出的物品数量（初始等于目标产出量）
                int remainingOutput = targetOutput;
                // 标记是否有过更新操作
                boolean hasUpdated = false;
                // 记录总共输出了多少物品
                int totalInserted = 0;
                // 记录实际消耗的能量
                int actualEnergyNeeded = 0;

                // 检查配置中是否启用了自动输出功能
                boolean autoOutputEnabled = ServerConfig.isItemReplicatorAutoOutputEnabled();

                // ========== 自动输出逻辑 ==========
                // 如果启用了自动输出
                if (autoOutputEnabled) {
                    // 获取所有六个方向（上、下、东、西、南、北）
                    Direction[] directions = Direction.values();

                    // 主动输出：每个 tick 只尝试输出到一个方向
                    for (Direction dir : directions) {
                        // 如果已经不需要再输出了，提前结束循环
                        if (remainingOutput <= 0) break;

                        // 获取相邻方块的坐标
                        BlockPos neighborPos = pos.relative(dir);
                        // 获取相邻方块的物品处理能力（从相反方向访问）
                        // 例如：从上方查询时，使用下方来访问邻居的能力
                        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());

                        // 如果邻居有物品处理能力
                        if (handler != null) {
                            // 计算要插入的数量（取剩余产出量和物品最大堆叠量的较小值）
                            int amountToInsert = Math.min(remainingOutput, inputStack.getMaxStackSize());
                            // 创建要输出的物品堆（复制输入物品，设置数量）
                            ItemStack outputStack = inputStack.copyWithCount(amountToInsert);
                            // 尝试向邻居插入物品，获取剩余的物品（没插进去的部分）
                            // insertItem 返回的是未被接受的物品
                            ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputStack, false);

                            // 计算实际插入的数量 = 尝试插入的数量 - 剩余的数量
                            int inserted = amountToInsert - remainder.getCount();

                            // 如果成功插入了物品
                            if (inserted > 0) {
                                // 累加到总输出量
                                totalInserted += inserted;
                                // 减少剩余待输出量
                                remainingOutput -= inserted;
                                // 标记已更新
                                hasUpdated = true;
                                // 累加能量消耗（每插入 1 个物品消耗 energyPerItem）
                                actualEnergyNeeded += inserted * energyPerItem;

                                // 主动输出只输出一个面就停止（找到第一个能接受的邻居就停止）
                                break;
                            }
                        }
                    }
                }

                // ========== 将剩余物品存入输出槽 ==========
                // 如果还有剩余物品没有输出出去
                if (remainingOutput > 0) {
                    // 遍历所有输出槽（从 OUTPUT_SLOT_START 开始，根据当前等级的输出槽数量确定结束位置）
                    // 条件是还有剩余物品且槽位未遍历完
                    for (int slot = OUTPUT_SLOT_START; slot < OUTPUT_SLOT_START + blockEntity.currentOutputSlots && remainingOutput > 0; slot++) {
                        // 获取当前输出槽的物品
                        ItemStack currentOutput = blockEntity.items[slot];

                        // 如果这个输出槽是空的
                        if (currentOutput.isEmpty()) {
                            // 复制输入物品
                            ItemStack newOutput = inputStack.copy();
                            // 计算能加入的数量（取剩余产出量和物品最大堆叠量的较小值）
                            int addAmount = Math.min(remainingOutput, inputStack.getMaxStackSize());
                            // 设置新物品堆的数量
                            newOutput.setCount(addAmount);
                            // 放入输出槽
                            blockEntity.items[slot] = newOutput;
                            // 累加到总输出量
                            totalInserted += addAmount;
                            // 减少剩余待输出量
                            remainingOutput -= addAmount;
                            // 标记已更新
                            hasUpdated = true;
                            // 累加能量消耗
                            actualEnergyNeeded += addAmount * energyPerItem;

                            // 能量检查：如果当前能量不足以支持下一个物品的消耗，提前退出循环
                            // actualEnergyNeeded + energyPerItem = 已计划消耗 + 下一个物品需要的能量
                            if (blockEntity.energyStored < actualEnergyNeeded + energyPerItem) {
                                break;
                            }
                        }
                        // 如果输出槽中的物品与当前产出的物品相同（比较物品 ID 和组件）
                        else if (ItemStack.isSameItemSameComponents(currentOutput, inputStack)) {
                            // 计算还能加入多少物品
                            // 取：(最大堆叠数 - 当前数量) 和 剩余产出量 的较小值
                            int canAdd = Math.min(
                                    inputStack.getMaxStackSize() - currentOutput.getCount(),
                                    remainingOutput
                            );

                            // 如果有空间可以加入
                            if (canAdd > 0) {
                                // 增加输出槽中的物品数量
                                currentOutput.grow(canAdd);
                                // 累加到总输出量
                                totalInserted += canAdd;
                                // 减少剩余待输出量
                                remainingOutput -= canAdd;
                                // 标记已更新
                                hasUpdated = true;
                                // 累加能量消耗
                                actualEnergyNeeded += canAdd * energyPerItem;

                                // 能量检查：如果当前能量不足以支持下一个物品的消耗，提前退出循环
                                if (blockEntity.energyStored < actualEnergyNeeded + energyPerItem) {
                                    break;
                                }
                            }
                        }
                    }
                }

                // ========== 最终检查和能量扣除 ==========
                // 如果有更新、总输出量大于 0、且需要消耗能量
                if (hasUpdated && totalInserted > 0 && actualEnergyNeeded > 0) {
                    // 再次检查能量是否足够（双重保险）
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
     * <p>
     * 此方法用于从物品复制机的输入槽抽取物品，通常在玩家取出物品时调用。
     * 它会清空输入槽并返回其中的物品堆。
     * </p>
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
}
