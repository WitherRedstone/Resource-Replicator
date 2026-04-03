package com.chinaex123.resource_replicator.network;

import com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record FluidSyncPacket(BlockPos pos, CompoundTag fluidData) implements CustomPacketPayload {
    
    public static final Type<@NotNull FluidSyncPacket> TYPE = new Type<>(
        Identifier.parse("resource_replicator:fluid_sync")
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSyncPacket> CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FluidSyncPacket::pos,
            ByteBufCodecs.COMPOUND_TAG,
            FluidSyncPacket::fluidData,
            FluidSyncPacket::new
        );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理流体同步数据包（在客户端执行）
     *
     * @param packet 流体同步数据包，包含方块位置和流体数据
     * @param context 载荷上下文，提供玩家和世界信息
     */
    public static void handle(FluidSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var clientLevel = context.player().level();
            if (clientLevel.isClientSide()) {
                var blockEntity = clientLevel.getBlockEntity(packet.pos);
                if (blockEntity instanceof FluidReplicatorBlockEntity replicator) {
                    replicator.loadFluidFromPacket(packet.fluidData);
                }
            }
        });
    }
}
