package zxc.rich.api.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;

import java.util.Random;

public interface Helper {

    Minecraft mc = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution(mc);

    default void sendPacket(Packet<?> packet) {
        mc.player.connection.sendPacket(packet);
    }

}
