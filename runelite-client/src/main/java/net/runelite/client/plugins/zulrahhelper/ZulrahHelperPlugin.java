/*
 * Copyright (c) 2017, AWPH-I
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
package net.runelite.client.plugins.zulrahhelper;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginToolbar;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(
		name = "Zulrah Helper",
		enabledByDefault = false
)
public class ZulrahHelperPlugin extends Plugin
{
	@Inject
	private PluginToolbar pluginToolbar;

	private NavigationButton navButton;

	private List<Integer> phases = new ArrayList<>();

	private static final int[] PATTERN_FOUR = {12, 4, 5, 6, 7, 8, 9, 10, 11, 10};
	private static final int[] PATTERN_THREE = {21, 22, 23, 24, 25, 26, 27, 28, 19};
	private static final int[] PATTERN_TWO = {30, 31, 32, 33, 34, 18};
	private static final int[] PATTERN_ONE = {13, 14, 15, 16, 17, 18};

	static final int[] FIRST_PHASE_CHOICES = {18, 20, 2};
	static final int[] SECOND_PHASE_CHOICES = {12, 29};

	@Override
	protected void startUp() throws Exception
	{
		final ZulrahHelperPanel panel = new ZulrahHelperPanel(this);

		BufferedImage icon;
		synchronized (ImageIO.class)
		{
			icon = ImageIO.read(getClass().getResourceAsStream("zulrah.png"));
		}

		navButton = NavigationButton.builder()
				.tooltip("Zulrah Helper")
				.icon(icon)
				.panel(panel)
				.build();

		pluginToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		pluginToolbar.removeNavigation(navButton);
	}

	void addPhase(int i)
	{
		phases.add(i);
	}

	int[] getPattern()
	{
		if (phases.size() == 0)
		{
			return null;
		}

		switch (phases.get(0))
		{
			case 20: return PATTERN_THREE;
			case 2: return PATTERN_FOUR;
			case 18: {
				if (phases.size() < 2)
				{
					return null;
				}

				return (phases.get(1) == 12) ? PATTERN_ONE : PATTERN_TWO;
			}
		}

		return null;
	}

	void resetPhases()
	{
		phases.clear();
	}
}
