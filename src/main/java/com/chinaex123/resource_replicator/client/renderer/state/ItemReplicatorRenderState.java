package com.chinaex123.resource_replicator.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class ItemReplicatorRenderState extends BlockEntityRenderState {
    public ItemStack displayedItem = ItemStack.EMPTY;
    public boolean hasItem = false;
}