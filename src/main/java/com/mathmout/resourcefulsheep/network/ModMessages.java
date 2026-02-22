package com.mathmout.resourcefulsheep.network;

import com.mathmout.resourcefulsheep.ResourcefulSheepMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ResourcefulSheepMod.MOD_ID)
public class ModMessages {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                SetDnaParentPayload.TYPE,
                SetDnaParentPayload.STREAM_CODEC,
                SetDnaParentPayload::handle
        );
    }
}
