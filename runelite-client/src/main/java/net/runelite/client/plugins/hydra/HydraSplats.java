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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class HydraSplats extends Overlay
{
	private final Client client;
	private final HydraConfig config;
	private final HydraPlugin plugin;

	@Inject
	private HydraSplats(Client client, HydraConfig config, HydraPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.setPosition(OverlayPosition.DYNAMIC);
		this.setPriority(OverlayPriority.LOW);
		this.setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public Dimension render(Graphics2D graphics)
	{
		List<WorldPoint> points = this.plugin.splats;
		if (points == null)
		{
			return null;
		}
		else
		{
			Iterator var3 = points.iterator();
			while (var3.hasNext())
			{
				WorldPoint point = (WorldPoint)var3.next();
				if (point.getPlane() == this.client.getPlane())
				{
					this.drawTile(graphics, point);
				}
			}

			return null;
		}
	}

	private void drawTile(Graphics2D graphics, WorldPoint point)
	{
		WorldPoint playerLocation = this.client.getLocalPlayer().getWorldLocation();
		if (point.distanceTo(playerLocation) < 32)
		{
			LocalPoint lp = LocalPoint.fromWorld(this.client, point);
			if (lp != null)
			{
				Polygon poly = Perspective.getCanvasTilePoly(this.client, lp);
				if (poly != null)
				{
					OverlayUtil.renderPolygon(graphics, poly, Color.YELLOW);
				}
			}
		}
	}
}

