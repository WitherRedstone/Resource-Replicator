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

/**
 * 流体同步数据包。
 * <p>
 * 用于将服务端流体复制机的流体数据同步到客户端，
 * 包含方块位置与流体数据的 NBT 标签。
 *
 * @param pos       方块位置
 * @param fluidData 流体数据
 */
public record FluidSyncPacket(BlockPos pos, CompoundTag fluidData) implements CustomPacketPayload {

    /**
     * 数据包类型标识。
     */
    public static final Type<@NotNull FluidSyncPacket> TYPE = new Type<>(
            Identifier.parse("resource_replicator:fluid_sync")
    );

    /**
     * 数据包编解码器。
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidSyncPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    FluidSyncPacket::pos,
                    ByteBufCodecs.COMPOUND_TAG,
                    FluidSyncPacket::fluidData,
                    FluidSyncPacket::new
            );

    /**
     * 获取数据包类型。
     *
     * @return 数据包类型
     */
    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理流体同步数据包。
     * <p>
     * 根据数据包中的方块位置获取客户端方块实体，
     * 并调用其 loadFluidFromPacket 方法加载流体数据。
     *
     * @param packet  流体同步数据包，包含方块位置和流体数据
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