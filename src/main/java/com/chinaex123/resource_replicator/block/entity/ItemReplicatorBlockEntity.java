package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.enumTier.ItemReplicatorTier;
import com.chinaex123.resource_replicator.config.ServerConfig;
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

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

    // 物品处理器 - 分离输入和输出
    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return TOTAL_SLOTS;
        }

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

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

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

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }
    };

    public ItemReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_REPLICATOR.get(), pos, state);
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("tickCounter", tickCounter);
        tag.putInt("tier", tier.getId());

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                itemsTag.put("item" + i, items[i].save(registries));
            }
        }
        tag.put("items", itemsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tickCounter = tag.getInt("tickCounter");
        if (tag.contains("tier")) {
            this.tier = ItemReplicatorTier.fromId(tag.getInt("tier"));
        }

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

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
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

    public void setTier(int tierId) {
        this.tier = ItemReplicatorTier.fromId(tierId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        if (blockEntity.tickCounter >= blockEntity.tier.getProcessSpeed()) {
            blockEntity.tickCounter = 0;

            ItemStack inputStack = blockEntity.items[INPUT_SLOT];
            if (!inputStack.isEmpty()) {
                int remainingOutput = blockEntity.tier.getOutputAmount();
                boolean hasUpdated = false;

                Direction[] directions = Direction.values();
                for (Direction dir : directions) {
                    if (remainingOutput <= 0) break;

                    BlockPos neighborPos = pos.relative(dir);
                    IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());

                    if (handler != null) {
                        ItemStack outputStack = inputStack.copy();
                        outputStack.setCount(1);

                        ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputStack, false);

                        if (remainder.isEmpty()) {
                            remainingOutput--;
                            hasUpdated = true;
                        }
                    }
                }

                if (remainingOutput > 0) {
                    for (int slot = OUTPUT_SLOT_START; slot < TOTAL_SLOTS && remainingOutput > 0; slot++) {
                        ItemStack currentOutput = blockEntity.items[slot];

                        if (currentOutput.isEmpty()) {
                            ItemStack newOutput = inputStack.copy();
                            newOutput.setCount(Math.min(remainingOutput, inputStack.getMaxStackSize()));
                            blockEntity.items[slot] = newOutput;
                            remainingOutput -= newOutput.getCount();
                            hasUpdated = true;
                        } else if (ItemStack.isSameItemSameComponents(currentOutput, inputStack)) {
                            int canAdd = Math.min(
                                    inputStack.getMaxStackSize() - currentOutput.getCount(),
                                    remainingOutput
                            );
                            if (canAdd > 0) {
                                currentOutput.grow(canAdd);
                                remainingOutput -= canAdd;
                                hasUpdated = true;
                            }
                        }
                    }
                }

                if (hasUpdated) {
                    blockEntity.markUpdated();
                }
            }
        }
    }

    public boolean addItem(ItemStack stack) {
        if (items[INPUT_SLOT].isEmpty()) {
            items[INPUT_SLOT] = stack.copy();
            markUpdated();
            return true;
        } else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
            int canAdd = stack.getMaxStackSize() - items[INPUT_SLOT].getCount();
            if (canAdd > 0) {
                items[INPUT_SLOT].grow(Math.min(canAdd, stack.getCount()));
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

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }
}
