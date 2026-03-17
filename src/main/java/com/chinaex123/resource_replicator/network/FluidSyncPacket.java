package com.chinaex123.resource_replicator.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidSyncPacket(BlockPos pos, CompoundTag fluidData) implements CustomPacketPayload {
    
    public static final Type<FluidSyncPacket> TYPE = new Type<>(
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
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FluidSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            if (level.isClientSide()) {
                var blockEntity = level.getBlockEntity(packet.pos);
                if (blockEntity instanceof com.chinaex123.resource_replicator.block.entity.FluidReplicatorBlockEntity replicator) {
                    replicator.loadFluidFromPacket(packet.fluidData);
                }
            }
        });
    }
}
