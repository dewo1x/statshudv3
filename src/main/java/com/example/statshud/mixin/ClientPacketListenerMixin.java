package com.example.statshud.mixin;

import com.example.statshud.PingTracker;
import com.example.statshud.TpsTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "handleSetTime", at = @At("HEAD"))
	private void statshud$onSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
		// The handler runs once on the network thread and again on the main thread; count it once.
		if (Minecraft.getInstance().isSameThread()) {
			TpsTracker.onTimePacket(packet.gameTime());
		}
	}

	@Inject(method = "handlePongResponse", at = @At("HEAD"), require = 0)
	private void statshud$onPong(ClientboundPongResponsePacket packet, CallbackInfo ci) {
		PingTracker.onPong(packet.time());
	}
}
