package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.ItemReplicatorBlock;
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
    private static final int TOTAL_SLOTS = 1 + MAX_OUTPUT_SLOTS + 1;
    private static final int VIRTUAL_SLOT = 10;
    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];

    private int tickCounter = 0;
    private ItemReplicatorTier tier = ItemReplicatorTier.ITEM_TIER_1;
    private int energyStored;
    private int energyCapacity;
    private int energyConsumption;
    private int currentOutputSlots;

    private boolean virtualSlotActive = false;
    private ItemResource virtualSlotResource = ItemResource.EMPTY;
    private int virtualSlotAmount = 0;
    private int virtualSlotAccumulator = 0;

    {
        updateEnergyStats();
        energyStored = 0;
        updateOutputSlots();
        items[VIRTUAL_SLOT] = ItemStack.EMPTY;
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

    private final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return TOTAL_SLOTS;
        }

        @Override
        public ItemResource getResource(int index) {
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

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            if (index == VIRTUAL_SLOT) {
                return tier.getOutputAmount();
            }
            return 64L;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index == VIRTUAL_SLOT) {
                return 0;
            }

            if (index == INPUT_SLOT) {
                if (resource.isEmpty()) {
                    return 0;
                }

                boolean isPipeInsertion = !isPlayerInsertion();

                if (isPipeInsertion) {
                    if (ServerConfig.isItemReplicatorDestroyEnabled()) {
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

            if (index >= OUTPUT_SLOT_START && index < TOTAL_SLOTS) {
                return 0;
            }

            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {

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

            if (index == INPUT_SLOT) {
                return 0;
            }

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

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (index == INPUT_SLOT) {
                ItemStack stack = resource.toStack(1);
                return ReplicatorFilter.canInsertItem(stack);
            }
            return true;
        }
    };

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

    public ItemReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_REPLICATOR.get(), pos, state);
        Arrays.fill(items, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
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
                itemTagOpt.ifPresent(itemTag -> {
                    ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> {
                        items[finalI] = stack;
                    });
                });
            } else {
            }
        }

    }

    @Override
    public void handleUpdateTag(net.minecraft.world.level.storage.ValueInput input) {
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());
        tag.putInt("energyStored", energyStored);

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            int finalI = i;
            ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, items[i]).result().ifPresent(itemTag -> {
                itemsTag.put("item" + finalI, itemTag);
            });
        }
        tag.put("items", itemsTag);

        setChanged();

        return tag;
    }

    public void handleUpdateTagFromPacket(net.minecraft.nbt.CompoundTag tag) {
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
                itemTagOpt.ifPresent(itemTag -> {
                    ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> {
                        items[finalI] = stack;
                    });
                });
            } else {
                items[finalI] = ItemStack.EMPTY;
            }
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            CompoundTag tag = getUpdateTag(level.registryAccess());

            var packet = new com.chinaex123.resource_replicator.network.ItemReplicatorSyncPacket(getBlockPos(), tag);

            ((net.minecraft.server.level.ServerLevel) level).getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(getBlockPos()), false)
                    .forEach(player -> net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet));

            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    public void setTier(int tierId) {
        this.tier = ItemReplicatorTier.fromId(tierId);
        updateEnergyStats();
        updateOutputSlots();
    }

    public int getCurrentOutputSlots() {
        return currentOutputSlots;
    }

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
                ResourceHandler<ItemResource> handler = level.getCapability(
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

    public ItemStack extractItem() {
        if (!items[INPUT_SLOT].isEmpty()) {
            ItemStack extracted = items[INPUT_SLOT].copy();

            items[INPUT_SLOT] = ItemStack.EMPTY;

            markUpdated();

            return extracted;
        }
        return ItemStack.EMPTY;
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    @Nullable
    public EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return energyHandler;
    }

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