/*
 * Copyright (c) 2019, awphi
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

package net.runelite.client.plugins.hydra;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
		name = "Hydra",
		description = "Helps you kill hydra",
		tags = {"hydra", "slayer", "pvm"}
)
public class HydraPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private HydraOverlay overlay;
	@Inject
	private Notifier notifier;
	@Inject
	private HydraConfig config;
	@Inject
	private SkillIconManager iconManager;
	@Inject
	private ChatMessageManager chatMessageManager;
	boolean projectileFinished;
	boolean projectileStarted;
	boolean prayMage;
	boolean prayRange;
	int mage = 0;
	int total = 0;
	int range = 0;
	int lastAttack = 0;
	int swapStyle = 2;
	boolean done;
	NPC hydra = null;
	List<WorldPoint> splats;
	final WorldArea hydraArea = new WorldArea(11488, 7583, 30, 30, 0);
	Projectile lastProjectile = null;
	BufferedImage rangeImage = ImageUtil.getResourceStreamFromClass(this.getClass(), "ranged.png");
	BufferedImage mageImage = ImageUtil.getResourceStreamFromClass(this.getClass(), "magic.png");
	String test = "Unknown";
	boolean hydraFound;

	@Provides
	HydraConfig provideConfig(ConfigManager configManager)
	{
		return (HydraConfig)configManager.getConfig(HydraConfig.class);
	}

	protected void startUp()
	{
		this.overlayManager.add(this.overlay);
		this.swapStyle = 2;
		this.done = true;
	}

	protected void shutDown() throws Exception
	{
		this.overlayManager.remove(this.overlay);
		this.mage = 0;
		this.range = 0;
		this.total = 0;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		this.hydraFound = false;
		List<NPC> npcs = this.client.getNpcs();
		if (npcs != null)
		{
			for (int i = 0; i < npcs.size(); ++i)
			{
				if (npcs.get(i).getName() != null && npcs.get(i).getName().equals("Alchemical Hydra"))
				{
					this.hydra = (NPC)npcs.get(i);
					this.hydraFound = true;
					break;
				}
			}
		}

		if (this.hydra != null && this.hydra.getId() == 8621 && this.done)
		{
			if (this.lastAttack == 1662)
			{
				this.prayMage = true;
				this.prayRange = false;
				this.mage = 1;
				this.range = 0;
			}

			if (this.lastAttack == 1663)
			{
				this.prayRange = true;
				this.prayMage = false;
				this.mage = 0;
				this.range = 1;
			}

			this.done = false;
		}

	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned spawned)
	{
		this.hydra = spawned.getNpc();
		this.mage = 0;
		this.range = 0;
		this.hydraFound = true;
		this.swapStyle = 2;
		this.total = 0;
		this.lastAttack = 0;
		this.lastProjectile = null;
		this.done = true;
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned despawned)
	{
		this.hydra = null;
		this.mage = 0;
		this.range = 0;
		this.hydraFound = false;
		this.swapStyle = 2;
		this.total = 0;
		this.lastAttack = 0;
		this.lastProjectile = null;
		this.done = true;
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphic)
	{
	}

	public void sendMsg(String msg)
	{
		String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append(msg).build();
		this.chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(chatMessage).build());
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved pq)
	{
		Projectile p = pq.getProjectile();
		if (this.config.debug())
		{
			this.sendMsg("Mage: " + this.mage);
			this.sendMsg("Range: " + this.range);
			this.sendMsg("prayMage: " + this.prayMage);
			this.sendMsg("prayRange: " + this.prayRange);
			this.sendMsg("swapStyle: " + this.swapStyle);
			this.sendMsg("_________________________________");
		}

		if (this.client.isInInstancedRegion())
		{
			if (this.hydra != null)
			{
				int lastPhaseId = 8621;
				if (this.hydra.getId() == lastPhaseId)
				{
					this.swapStyle = 0;
					if (this.lastAttack == 1662)
					{
						this.range = 1;
						this.mage = 0;
					}

					if (this.lastAttack == 1663)
					{
						this.mage = 1;
						this.range = 0;
					}
				}
				else
				{
					this.swapStyle = 2;
				}

				Player player = this.client.getLocalPlayer();
				int cycle = p.getStartMovementCycle();
				int finishCycle = p.getEndCycle();
				int id = p.getId();
				int cyclesLeft = p.getRemainingCycles();
				if (id == 1662 || id == 1663)
				{
					if (this.client.getGameCycle() >= p.getStartMovementCycle())
					{
						return;
					}

					if (this.lastProjectile != null && p.getStartMovementCycle() == this.lastProjectile.getStartMovementCycle())
					{
						return;
					}

					if (id == 1663)
					{
						++this.range;
						++this.total;
					}

					if (id == 1662)
					{
						++this.mage;
						++this.total;
					}

					this.lastAttack = id;
					if (this.range > this.swapStyle)
					{
						this.prayMage = true;
						this.prayRange = false;
						this.range = 0;
						this.mage = 0;
						this.test = "Pray Mage";
					}

					if (this.mage > this.swapStyle)
					{
						this.prayMage = false;
						this.prayRange = true;
						this.mage = 0;
						this.range = 0;
						this.test = "Pray Range";
					}

					if (this.total < 3)
					{
						if (id == 1662)
						{
							this.test = "Pray Mage";
							this.prayMage = true;
							this.prayRange = false;
						}

						if (id == 1663)
						{
							this.test = "Pray Range";
							this.prayMage = false;
							this.prayRange = true;
						}
					}

					if (this.config.chat())
					{
						String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.NORMAL).append(this.test).build();
						this.chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(chatMessage).build());
					}

					this.lastProjectile = p;
				}

			}
		}
	}
}
