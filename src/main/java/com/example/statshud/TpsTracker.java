package com.example.statshud;

import java.util.ArrayDeque;

/** Estimates server TPS from how fast the server's game time advances in real time. */
public final class TpsTracker {
	private static final long WINDOW_NANOS = 15_000_000_000L;
	private static final long STALE_NANOS = 5_000_000_000L;
	private static final ArrayDeque<long[]> SAMPLES = new ArrayDeque<>(); // {nanos, gameTime}

	private TpsTracker() {}

	public static void reset() {
		SAMPLES.clear();
	}

	public static void onTimePacket(long gameTime) {
		long now = System.nanoTime();
		long[] last = SAMPLES.peekLast();
		if (last != null && gameTime < last[1]) {
			SAMPLES.clear(); // world/time jumped backwards
		}
		SAMPLES.addLast(new long[] {now, gameTime});
		while (SAMPLES.size() > 2 && now - SAMPLES.peekFirst()[0] > WINDOW_NANOS) {
			SAMPLES.removeFirst();
		}
	}

	/** @return TPS (max 20) or NaN if there is not enough data yet. */
	public static double getTps() {
		if (SAMPLES.size() < 2) return Double.NaN;
		long[] first = SAMPLES.peekFirst();
		long[] last = SAMPLES.peekLast();
		if (System.nanoTime() - last[0] > STALE_NANOS) return Double.NaN;
		double seconds = (last[0] - first[0]) / 1_000_000_000.0;
		if (seconds <= 0) return Double.NaN;
		double tps = (last[1] - first[1]) / seconds;
		return Math.max(0, Math.min(20.0, tps));
	}
}
