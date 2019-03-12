/*
 * Copyright (c) 2018 AWPH-I
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.inventoryviewer;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.party.PartyPluginService;
import net.runelite.client.plugins.party.data.ItemData;
import net.runelite.client.plugins.party.data.PartyData;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class InventoryViewerOverlay extends Overlay
{
	private static final int INVENTORY_SIZE = 28;
	private static final int PLACEHOLDER_WIDTH = 36;
	private static final int PLACEHOLDER_HEIGHT = 32;
	private static final ImageComponent PLACEHOLDER_IMAGE = new ImageComponent(new BufferedImage(PLACEHOLDER_WIDTH, PLACEHOLDER_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));

	private final Client client;
	private final ItemManager itemManager;
	private final PartyPluginService partyService;

	private final PanelComponent inventoryPanel = new PanelComponent();
	private final PanelComponent inventoryWrapper = new PanelComponent();

	@Inject
	private InventoryViewerOverlay(final Client client, final InventoryViewerPlugin plugin, final ItemManager itemManager, final PartyPluginService partyService)
	{
		super(plugin);

		setPosition(OverlayPosition.BOTTOM_RIGHT);
		inventoryPanel.setWrapping(4);

		inventoryPanel.setGap(new Point(6, 4));
		inventoryPanel.setOrientation(PanelComponent.Orientation.HORIZONTAL);
		inventoryPanel.setBackgroundColor(null);

		inventoryWrapper.setWrapping(2);
		inventoryWrapper.setGap(new Point(0, 4));
		inventoryWrapper.setOrientation(PanelComponent.Orientation.VERTICAL);

		this.client = client;
		this.itemManager = itemManager;
		this.partyService = partyService;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final PartyData member = partyService.getHoveredPartyMember();

		final ItemData[] items;
		String name;

		if (member == null)
		{
			final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);

			if (itemContainer == null)
			{
				return null;
			}

			final Item[] x = itemContainer.getItems();
			items = new ItemData[INVENTORY_SIZE];

			for (int i = 0; i < INVENTORY_SIZE; i ++)
			{
				if (i < x.length)
				{
					items[i] = new ItemData(x[i].getId(), x[i].getQuantity());
					continue;
				}

				items[i] = new ItemData(-1, 0);
			}

			name = "Your";
		}
		else
		{
			items = member.getInventory();

			name = member.getName().split("#")[0];
			name += name.endsWith("s") ? "'" : "'s";
		}

		inventoryWrapper.getChildren().clear();
		inventoryPanel.getChildren().clear();

		inventoryWrapper.getChildren().add(
				TitleComponent.builder()
						.text(name + " Inventory")
						.build()
		);

		for (int i = 0; i < INVENTORY_SIZE; i++)
		{
			final ItemData item = items[i];
			if (item.getQuantity() > 0)
			{
				final BufferedImage image = getImage(item);
				if (image != null)
				{
					inventoryPanel.getChildren().add(new ImageComponent(image));
					continue;
				}
			}

			// put a placeholder image so each item is aligned properly and the panel is not resized
			inventoryPanel.getChildren().add(PLACEHOLDER_IMAGE);
		}

		inventoryWrapper.getChildren().add(inventoryPanel);

		return inventoryWrapper.render(graphics);
	}

	private BufferedImage getImage(ItemData item)
	{
		ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
		return itemManager.getImage(item.getId(), item.getQuantity(), itemComposition.isStackable());
	}
}
