package com.jhonatan.arcanefamiliar.network;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ArcaneFamiliar.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SealParchmentC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SealParchmentC2SPacket::new)
                .encoder(SealParchmentC2SPacket::toBytes)
                .consumerMainThread(SealParchmentC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}