package com.chinaex123.resource_replicator.network;

import com.chinaex123.resource_replicator.block.entity.ItemReplicatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ItemReplicatorSyncPacket(BlockPos pos, CompoundTag data) implements CustomPacketPayload {
    
    public static final Type<@NotNull ItemReplicatorSyncPacket> TYPE = new Type<>(
        Identifier.parse("resource_replicator:item_replicator_sync")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemReplicatorSyncPacket> CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ItemReplicatorSyncPacket::pos,
            ByteBufCodecs.COMPOUND_TAG,
            ItemReplicatorSyncPacket::data,
            ItemReplicatorSyncPacket::new
        );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理物品复制机同步数据包（在客户端执行）
     *
     * @param packet 物品复制机同步数据包，包含方块位置和 NBT 数据
     * @param context 载荷上下文，提供玩家和世界信息
     */
    public static void handle(ItemReplicatorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var clientLevel = context.player().level();
            if (clientLevel.isClientSide()) {
                var blockEntity = clientLevel.getBlockEntity(packet.pos);
                if (blockEntity instanceof ItemReplicatorBlockEntity replicator) {
                    replicator.handleUpdateTagFromPacket(packet.data);
                }
            }
        });
    }
}
