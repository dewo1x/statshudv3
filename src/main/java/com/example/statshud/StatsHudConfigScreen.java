package com.example.statshud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class StatsHudConfigScreen extends Screen {
	private static final int ROW = 24;
	private static final Identifier LOGO = Identifier.fromNamespaceAndPath(StatsHudClient.MOD_ID, "textures/gui/logo.png");
	private static final int LOGO_W = 132;
	private static final int LOGO_H = 10;

	private final Screen parent;

	public StatsHudConfigScreen(Screen parent) {
		super(Component.literal("Stats HUD"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int left = cx - 102;
		int right = cx + 2;
		int y = Math.max(30, this.height / 2 - 96);

		toggle(left, y, 100, "Show FPS", () -> StatsHudConfig.showFps, v -> StatsHudConfig.showFps = v);
		toggle(right, y, 100, "Show Ping", () -> StatsHudConfig.showPing, v -> StatsHudConfig.showPing = v);
		y += ROW;

		toggle(left, y, 100, "Show TPS", () -> StatsHudConfig.showTps, v -> StatsHudConfig.showTps = v);
		toggle(right, y, 100, "Shadow", () -> StatsHudConfig.shadow, v -> StatsHudConfig.shadow = v);
		y += ROW;

		colorButton(left, y, 100, "Labels", () -> StatsHudConfig.labelColor, v -> StatsHudConfig.labelColor = v);
		colorButton(right, y, 100, "Values", () -> StatsHudConfig.valueColor, v -> StatsHudConfig.valueColor = v);
		y += ROW;

		toggle(left, y, 204, "Color values by performance", () -> StatsHudConfig.performanceColors,
				v -> StatsHudConfig.performanceColors = v);
		y += ROW;

		stepper(cx, y, "Size", () -> StatsHudConfig.sizePercent, v -> StatsHudConfig.sizePercent = v, 10, 50, 300, "%");
		y += ROW;
		stepper(cx, y, "X position", () -> StatsHudConfig.offsetX, v -> StatsHudConfig.offsetX = v, 5, 0, 800, "");
		y += ROW;
		stepper(cx, y, "Y position", () -> StatsHudConfig.offsetY, v -> StatsHudConfig.offsetY = v, 5, 0, 800, "");
		y += ROW;

		this.addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
			StatsHudConfig.resetDefaults();
			StatsHudConfig.save();
			Minecraft.getInstance().gui.setScreen(new StatsHudConfigScreen(parent));
		}).bounds(left, y, 100, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
				.bounds(right, y, 100, 20).build());
	}

	private void toggle(int x, int y, int w, String name, BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
		this.addRenderableWidget(Button.builder(toggleText(name, get.getAsBoolean()), b -> {
			set.accept(!get.getAsBoolean());
			b.setMessage(toggleText(name, get.getAsBoolean()));
			StatsHudConfig.save();
		}).bounds(x, y, w, 20).build());
	}

	private static Component toggleText(String name, boolean on) {
		return Component.literal(name + ": " + (on ? "ON" : "OFF"));
	}

	private void colorButton(int x, int y, int w, String name, IntSupplier get, IntConsumer set) {
		this.addRenderableWidget(Button.builder(colorText(name, get.getAsInt()), b -> {
			set.accept((get.getAsInt() + 1) % StatsHudConfig.PALETTE.length);
			b.setMessage(colorText(name, get.getAsInt()));
			StatsHudConfig.save();
		}).bounds(x, y, w, 20).build());
	}

	private static Component colorText(String name, int index) {
		return Component.literal(name + ": " + StatsHudConfig.PALETTE[index].name());
	}

	private void stepper(int cx, int y, String name, IntSupplier get, IntConsumer set, int step, int min, int max, String suffix) {
		Button valueButton = Button.builder(Component.literal(name + ": " + get.getAsInt() + suffix), b -> {})
				.bounds(cx - 80, y, 160, 20).build();
		valueButton.active = false;

		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			set.accept(StatsHudConfig.clamp(get.getAsInt() - step, min, max));
			valueButton.setMessage(Component.literal(name + ": " + get.getAsInt() + suffix));
			StatsHudConfig.save();
		}).bounds(cx - 102, y, 20, 20).build());

		this.addRenderableWidget(valueButton);

		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			set.accept(StatsHudConfig.clamp(get.getAsInt() + step, min, max));
			valueButton.setMessage(Component.literal(name + ": " + get.getAsInt() + suffix));
			StatsHudConfig.save();
		}).bounds(cx + 82, y, 20, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		String title = "Stats HUD";
		graphics.text(this.font, title, (this.width - this.font.width(title)) / 2, 12,
				StatsHudConfig.PALETTE[0].argb(), true);

		// logo (texture is 264x20, drawn at half size)
		graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, (this.width - LOGO_W) / 2, this.height - 32,
				0, 0, LOGO_W, LOGO_H, 264, 20, 264, 20);

		String credit = "Made by Dewoix";
		graphics.text(this.font, credit, (this.width - this.font.width(credit)) / 2, this.height - 16, 0xFFAAAAAA, true);
	}

	@Override
	public void onClose() {
		StatsHudConfig.save();
		Minecraft.getInstance().gui.setScreen(parent);
	}
}
