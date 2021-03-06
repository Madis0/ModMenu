package io.github.prospector.modmenu.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.prospector.modmenu.util.RenderUtils;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.TextFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class ModListWidget extends EntryListWidget {
	private static final Logger LOGGER = LogManager.getLogger();

	private List<ModContainer> modInfoList = null;
	public ModEntryWidget selected;
	public ModListGui gui;

	public ModListWidget(MinecraftClient client,
	                     int width,
	                     int height,
	                     int y1,
	                     int y2,
	                     int entryHeight,
	                     Supplier<String> searchTerm, ModListWidget list, ModListGui gui) {
		super(client, width, height, y1, y2, entryHeight);
		if (list != null) {
			this.modInfoList = list.modInfoList;
		}
		this.selected = null;
		this.searchFilter(searchTerm, false);
		this.gui = gui;
	}

	@Override
	public void draw(int i, int i1, float v) {
		super.draw(i, i1, v);
		if (selected != null) {
			ModInfo info = selected.info;
			int x = width + 8;
			int y = y1;
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.client.getTextureManager().bindTexture(selected.nativeImageBackedTexture != null ? selected.iconLocation : ModEntryWidget.unknownIcon);
			GlStateManager.enableBlend();
			Drawable.drawTexturedRect(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
			GlStateManager.disableBlend();
			int lineSpacing = client.fontRenderer.fontHeight + 1;
			int imageOffset = 36;
			this.client.fontRenderer.draw(info.getName(), x + imageOffset, y, 0xFFFFFF);
			this.client.fontRenderer.draw(" (ID: " + info.getId() + ")", x + imageOffset + client.fontRenderer.getStringWidth(info.getName()), y, 0xAAAAAA);
			this.client.fontRenderer.draw("v" + info.getVersionString(), x + imageOffset, y + lineSpacing, 0xAAAAAA);
			if (info.getLinks().getHomepage() != null && !info.getLinks().getHomepage().isEmpty()) {
				this.client.fontRenderer.draw(TextFormat.BLUE + "" + TextFormat.UNDERLINE + info.getLinks().getHomepage(), x + imageOffset, y + lineSpacing * 2, 0);
			}
			y = y1 + imageOffset + 24;
			if (info.getDescription() != null && !info.getDescription().isEmpty()) {
				RenderUtils.drawWrappedString(info.getDescription(), x, y, gui.width - this.width - 20, 5, 0xAAAAAA);
			}
		}
	}

	@Override
	public int getEntryWidth() {
		return this.width - 8;
	}

	public void searchFilter(Supplier<String> searchTerm, boolean var2) {
		this.clearEntries();
		List<ModContainer> mods = FabricLoader.INSTANCE.getMods();
		if (this.modInfoList == null || var2) {
			this.modInfoList = new ArrayList<>();
			modInfoList.addAll(mods);
			this.modInfoList.sort(Comparator.comparing(modContainer -> modContainer.getInfo().getName()));
		}

		String term = searchTerm.get().toLowerCase(Locale.ROOT);
		Iterator<ModContainer> iter = this.modInfoList.iterator();

		while (true) {
			ModContainer container;
			ModInfo info;
			do {
				if (!iter.hasNext()) {
					return;
				}
				container = iter.next();
				info = container.getInfo();
			} while (!info.getName().toLowerCase(Locale.ROOT).contains(term) && !info.getId().toLowerCase(Locale.ROOT).contains(term) && !info.getAuthors().stream().anyMatch(person -> person.getName().equalsIgnoreCase(term)));

			this.addEntry(new ModEntryWidget(container, this));
		}
	}

	@Override
	protected int getScrollbarPosition() {
		return width - 6;
	}

	public int getWidth() {
		return width;
	}

	public int getY1() {
		return this.y1;
	}

	public int getY2() {
		return this.y2;
	}
}
