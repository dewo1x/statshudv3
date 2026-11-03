package com.example.statshud;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Simple settings stored in config/statshud.properties. */
public final class StatsHudConfig {
	public record NamedColor(String name, int argb) {}

	public static final NamedColor[] PALETTE = {
			new NamedColor("Blue", 0xFF4DA6FF),
			new NamedColor("Dark Blue", 0xFF5555FF),
			new NamedColor("Aqua", 0xFF55FFFF),
			new NamedColor("Green", 0xFF55FF55),
			new NamedColor("Yellow", 0xFFFFFF55),
			new NamedColor("Gold", 0xFFFFAA00),
			new NamedColor("Red", 0xFFFF5555),
			new NamedColor("Pink", 0xFFFF55FF),
			new NamedColor("White", 0xFFFFFFFF),
			new NamedColor("Gray", 0xFFAAAAAA),
	};

	public static boolean showFps = true;
	public static boolean showPing = true;
	public static boolean showTps = true;
	public static boolean shadow = true;
	public static boolean performanceColors = false;
	public static int sizePercent = 100;
	public static int offsetX = 3;
	public static int offsetY = 3;
	public static int labelColor = 0; // Blue
	public static int valueColor = 8; // White

	private StatsHudConfig() {}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("statshud.properties");
	}

	public static void resetDefaults() {
		showFps = true;
		showPing = true;
		showTps = true;
		shadow = true;
		performanceColors = false;
		sizePercent = 100;
		offsetX = 3;
		offsetY = 3;
		labelColor = 0;
		valueColor = 8;
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			save();
			return;
		}
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			Properties p = new Properties();
			p.load(reader);
			showFps = Boolean.parseBoolean(p.getProperty("showFps", "true"));
			showPing = Boolean.parseBoolean(p.getProperty("showPing", "true"));
			showTps = Boolean.parseBoolean(p.getProperty("showTps", "true"));
			shadow = Boolean.parseBoolean(p.getProperty("shadow", "true"));
			performanceColors = Boolean.parseBoolean(p.getProperty("performanceColors", "false"));
			sizePercent = clamp(parse(p, "sizePercent", 100), 50, 300);
			offsetX = clamp(parse(p, "offsetX", 3), 0, 800);
			offsetY = clamp(parse(p, "offsetY", 3), 0, 800);
			labelColor = clamp(parse(p, "labelColor", 0), 0, PALETTE.length - 1);
			valueColor = clamp(parse(p, "valueColor", 8), 0, PALETTE.length - 1);
		} catch (Exception e) {
			System.err.println("[statshud] Could not read config, using defaults: " + e);
			resetDefaults();
		}
	}

	public static void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(file())) {
			Properties p = new Properties();
			p.setProperty("showFps", Boolean.toString(showFps));
			p.setProperty("showPing", Boolean.toString(showPing));
			p.setProperty("showTps", Boolean.toString(showTps));
			p.setProperty("shadow", Boolean.toString(shadow));
			p.setProperty("performanceColors", Boolean.toString(performanceColors));
			p.setProperty("sizePercent", Integer.toString(sizePercent));
			p.setProperty("offsetX", Integer.toString(offsetX));
			p.setProperty("offsetY", Integer.toString(offsetY));
			p.setProperty("labelColor", Integer.toString(labelColor));
			p.setProperty("valueColor", Integer.toString(valueColor));
			p.store(writer, "Stats HUD settings - made by Dewoix");
		} catch (Exception e) {
			System.err.println("[statshud] Could not save config: " + e);
		}
	}

	private static int parse(Properties p, String key, int fallback) {
		try {
			return Integer.parseInt(p.getProperty(key, Integer.toString(fallback)).trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
