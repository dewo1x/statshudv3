package com.example.statshud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;

import java.util.ArrayDeque;

/**
 * Measures real round-trip time by sending our own ping packets several times per second.
 * (The tab-list latency vanilla exposes only refreshes about every 30 seconds.)
 */
public final class PingTracker {
	private static final int INTERVAL_TICKS = 5; // 4 pings per second
	private static final int SAMPLES = 4;        // average over ~1 second
	private static final long STALE_MS = 5000;

	private static final ArrayDeque<Long> RTT = new ArrayDeque<>();
	private static long lastPongMs = 0;
	private static long lastSeenTime = Long.MIN_VALUE;
	private static int tickCounter = 0;

	private PingTracker() {}

	private static long nowMs() {
		return System.nanoTime() / 1_000_000L;
	}

	public static synchronized void reset() {
		RTT.clear();
		lastPongMs = 0;
		lastSeenTime = Long.MIN_VALUE;
		tickCounter = 0;
	}

	/** Called every client tick. */
	public static void tick(Minecraft mc) {
		ClientPacketListener conn = mc.getConnection();
		if (conn == null || mc.player == null) return;
		if (++tickCounter >= INTERVAL_TICKS) {
			tickCounter = 0;
			conn.send(new ServerboundPingRequestPacket(nowMs()));
		}
	}

	/** Called when a pong arrives; sentMs is the value we put in the request. */
	public static synchronized void onPong(long sentMs) {
		if (sentMs == lastSeenTime) return; // the handler can run twice per packet
		lastSeenTime = sentMs;
		long now = nowMs();
		RTT.addLast(Math.max(0, now - sentMs));
		while (RTT.size() > SAMPLES) RTT.removeFirst();
		lastPongMs = now;
	}

	/** @return ping in ms, or -1 if there is no fresh measurement. */
	public static synchronized int getPing() {
		if (RTT.isEmpty() || nowMs() - lastPongMs > STALE_MS) return -1;
		long sum = 0;
		for (long v : RTT) sum += v;
		return (int) Math.round(sum / (double) RTT.size());
	}
}
