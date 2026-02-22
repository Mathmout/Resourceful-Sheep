package com.mathmout.resourcefulsheep.network;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import com.mathmout.resourcefulsheep.block.entity.DNASplicerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SetDnaParentPayload(BlockPos pos, int slotId, String dnaId) implements CustomPacketPayload {

    public static final Type<SetDnaParentPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ResourcefulSheepMod.MOD_ID, "set_dna_parent"));

    public static final StreamCodec<ByteBuf, SetDnaParentPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetDnaParentPayload::pos,
            ByteBufCodecs.VAR_INT, SetDnaParentPayload::slotId,
            ByteBufCodecs.STRING_UTF8, SetDnaParentPayload::dnaId,
            SetDnaParentPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final SetDnaParentPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();
                if (level.isLoaded(payload.pos)) {
                    if (level.getBlockEntity(payload.pos) instanceof DNASplicerBlockEntity splicer) {
                        if (payload.slotId == 1) {
                            splicer.setMom_id(payload.dnaId);
                        } else if (payload.slotId == 2) {
                            splicer.setDad_id(payload.dnaId);
                        }
                    }
                }
            }
        });
    }
}
