package com.chinaex123.resource_replicator.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

/**
 * 物品复制机渲染状态。
 * <p>
 * 用于在客户端渲染物品复制机时保存需要渲染的数据，
 * 包括当前输入槽中显示的物品以及是否有物品的标志。
 */
public class ItemReplicatorRenderState extends BlockEntityRenderState {

    /**
     * 要显示的物品（输入槽的物品）。
     */
    public ItemStack displayedItem = ItemStack.EMPTY;

    /**
     * 是否有物品的标志。
     */
    public boolean hasItem = false;
}