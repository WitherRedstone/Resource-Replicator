package com.chinaex123.resource_replicator.block.entity;

import com.chinaex123.resource_replicator.block.ReplicatorTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class ResourceReplicatorBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INPUT_SLOT = 0;
    private static final int TOTAL_SLOTS = 1;

    public final ItemStack[] items = new ItemStack[TOTAL_SLOTS];
    private int tickCounter = 0;
    private ReplicatorTier tier = ReplicatorTier.TIER_1;

    public ResourceReplicatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESOURCE_REPLICATOR.get(), pos, state);
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
            this.tier = ReplicatorTier.fromId(tag.getInt("tier"));
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

    public void setTier(int tierId) {
        this.tier = ReplicatorTier.fromId(tierId);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResourceReplicatorBlockEntity blockEntity) {
        blockEntity.tickCounter++;

        if (blockEntity.tickCounter >= blockEntity.tier.getProcessSpeed()) {
            blockEntity.tickCounter = 0;

            ItemStack inputStack = blockEntity.items[INPUT_SLOT];
            if (!inputStack.isEmpty()) {
                int remainingOutput = blockEntity.tier.getOutputAmount();

                boolean[] fullSides = new boolean[6];
                int activeSidesCount = 0;

                for (int i = 0; i < 6; i++) {
                    Direction dir = Direction.values()[i];
                    BlockPos neighborPos = pos.relative(dir);

                    IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());
                    if (handler != null) {
                        activeSidesCount++;
                    } else {
                        fullSides[i] = true;
                    }
                }

                if (activeSidesCount == 0) {
                    return;
                }

                while (remainingOutput > 0 && activeSidesCount > 0) {
                    boolean anyInserted = false;

                    for (int i = 0; i < 6; i++) {
                        if (fullSides[i] || remainingOutput <= 0) continue;

                        Direction dir = Direction.values()[i];
                        BlockPos neighborPos = pos.relative(dir);

                        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, neighborPos, dir.getOpposite());

                        if (handler != null) {
                            ItemStack outputStack = inputStack.copy();
                            outputStack.setCount(1);

                            ItemStack remainder = ItemHandlerHelper.insertItem(handler, outputStack, false);

                            if (remainder.isEmpty()) {
                                remainingOutput--;
                                anyInserted = true;
                            } else {
                                fullSides[i] = true;
                                activeSidesCount--;
                            }
                        }
                    }

                    if (!anyInserted && activeSidesCount > 0) {
                        break;
                    }
                }
            }
        }
    }

    public boolean addItem(ItemStack stack) {
        if (items[INPUT_SLOT].isEmpty()) {
            items[INPUT_SLOT] = stack.copy();
            setChanged();
            return true;
        } else if (ItemStack.isSameItemSameComponents(items[INPUT_SLOT], stack)) {
            int canAdd = stack.getMaxStackSize() - items[INPUT_SLOT].getCount();
            if (canAdd > 0) {
                items[INPUT_SLOT].grow(Math.min(canAdd, stack.getCount()));
                setChanged();
                return true;
            }
        }
        return false;
    }

    public ItemStack getDisplayedItem() {
        return items[INPUT_SLOT];
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.resource_replicator.resource_replicator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return null;
    }
}
