package com.example.statshud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;

public class StatsHudClient implements ClientModInitializer {
	public static final String MOD_ID = "statshud";

	private static final int LINE_HEIGHT = 10;
	private static final int GREEN = 0xFF55FF55;
	private static final int YELLOW = 0xFFFFFF55;
	private static final int RED = 0xFFFF5555;
	private static final int GRAY = 0xFFAAAAAA;

	@Override
	public void onInitializeClient() {
		StatsHudConfig.load();
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "overlay"), StatsHudClient::extract);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			TpsTracker.reset();
			PingTracker.reset();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			TpsTracker.reset();
			PingTracker.reset();
		});
		ClientTickEvents.END_CLIENT_TICK.register(PingTracker::tick);
	}

	private static void extract(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		Font font = mc.font;
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(StatsHudConfig.offsetX, StatsHudConfig.offsetY);
		float scale = StatsHudConfig.sizePercent / 100f;
		pose.scale(scale, scale);

		int row = 0;

		if (StatsHudConfig.showFps) {
			int fps = mc.getFps();
			line(g, font, row++, "FPS: ", Integer.toString(fps), fps >= 60 ? GREEN : fps >= 30 ? YELLOW : RED);
		}

		if (StatsHudConfig.showPing) {
			int ping = PingTracker.getPing(); // live measurement
			ClientPacketListener conn = mc.getConnection();
			if (ping < 0 && conn != null) { // fall back to the tab-list value
				PlayerInfo info = conn.getPlayerInfo(mc.player.getUUID());
				if (info != null) ping = info.getLatency();
			}
			line(g, font, row++, "Ping: ", ping >= 0 ? ping + " ms" : "--",
					ping < 0 ? GRAY : ping <= 80 ? GREEN : ping <= 200 ? YELLOW : RED);
		}

		if (StatsHudConfig.showTps) {
			double tps = TpsTracker.getTps();
			line(g, font, row++, "TPS: ", Double.isNaN(tps) ? "--" : String.format("%.1f", tps),
					Double.isNaN(tps) ? GRAY : tps >= 18 ? GREEN : tps >= 12 ? YELLOW : RED);
		}

		pose.popMatrix();
	}

	private static void line(GuiGraphicsExtractor g, Font font, int row, String label, String value, int performanceColor) {
		int y = row * LINE_HEIGHT;
		boolean shadow = StatsHudConfig.shadow;
		int labelColor = StatsHudConfig.PALETTE[StatsHudConfig.labelColor].argb();
		int valueColor = StatsHudConfig.performanceColors
				? performanceColor
				: StatsHudConfig.PALETTE[StatsHudConfig.valueColor].argb();

		g.text(font, label, 0, y, labelColor, shadow);
		g.text(font, value, font.width(label), y, valueColor, shadow);
	}
}
