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

import net.runelite.client.ui.PluginPanel;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ZulrahHelperPanel extends PluginPanel
{
	private JPanel choicePanel;
	private JPanel patternPanel;

	private Map<JButton, Integer> phaseChoices = new HashMap<>();

	private ZulrahHelperPlugin plugin;

	ZulrahHelperPanel(ZulrahHelperPlugin plugin)
	{
		this.plugin = plugin;

		setLayout(new BorderLayout(3, 3));

		JPanel topPanel = new JPanel(new BorderLayout());

		topPanel.add(new JLabel("Zulrah Helper", SwingConstants.CENTER), BorderLayout.NORTH);
		topPanel.add(new JLabel("Current phase:"), BorderLayout.WEST);

		choicePanel = new JPanel(new GridLayout(1, 3));
		choicePanel.setBorder(BorderFactory.createLineBorder(getBackground().brighter(), 1, true));
		topPanel.add(choicePanel, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());

		centerPanel.add(new JLabel("Current pattern:"), BorderLayout.WEST);

		patternPanel = new JPanel(new GridLayout(0, 3, 4, 4));
		centerPanel.add(patternPanel, BorderLayout.SOUTH);

		add(centerPanel, BorderLayout.CENTER);

		JButton resetBtn = new JButton("Reset");
		resetBtn.addActionListener(e -> reset());
		add(resetBtn, BorderLayout.SOUTH);

		reset();
	}

	private void reset()
	{
		plugin.resetPhases();
		choicePanel.removeAll();
		patternPanel.removeAll();
		drawPhase(1, true);

		for (int FIRST_PHASE_CHOICE : ZulrahHelperPlugin.FIRST_PHASE_CHOICES)
		{
			JButton button = new JButton(new ImageIcon(getPhaseImage(FIRST_PHASE_CHOICE)));
			button.addActionListener(e -> phaseClicked(phaseChoices.get((JButton) e.getSource())));
			choicePanel.add(button);
			phaseChoices.put(button, FIRST_PHASE_CHOICE);
		}

		setChoicesEnabled(true);

		updateUI();
	}

	private void phaseClicked(int phClicked)
	{
		plugin.addPhase(phClicked);

		drawPhase(phClicked, true);
		phaseChoices.clear();

		if (plugin.getPattern() == null)
		{
			drawPhase(19, false);

			choicePanel.removeAll();

			for (int SECOND_PHASE_CHOICE : ZulrahHelperPlugin.SECOND_PHASE_CHOICES)
			{
				JButton button = new JButton(new ImageIcon(getPhaseImage(SECOND_PHASE_CHOICE)));
				button.addActionListener(e -> phaseClicked(phaseChoices.get((JButton) e.getSource())));
				choicePanel.add(button);
				phaseChoices.put(button, SECOND_PHASE_CHOICE);
			}
		}
		else
		{
			setChoicesEnabled(false);
			loadPattern(plugin.getPattern());
		}

		updateUI();
	}

	private void setChoicesEnabled(boolean a)
	{
		for (Component i : choicePanel.getComponents())
		{
			i.setEnabled(a);
		}
	}

	private void drawPhase(int i, boolean done)
	{
		BufferedImage image = getPhaseImage(i);
		if (image == null)
		{
			return;
		}

		JLabel label = new JLabel(new ImageIcon(image));

		if (done)
		{
			label.setOpaque(true);
			label.setBackground(new Color(0, 0, 0, 0.5f));
		}

		label.setBorder(BorderFactory.createLineBorder(getBackground().brighter(), 1, true));
		patternPanel.add(label);
	}

	private BufferedImage getPhaseImage(int i)
	{
		BufferedImage icon = null;
			try
			{
				icon = ImageIO.read(getClass().getResourceAsStream("phases/zul-" + i + ".png"));
			}
				catch (IOException ignored)
			{

			}

		return icon;
	}

	private void loadPattern(int[] pattern)
	{
		for (int i : pattern)
		{
			drawPhase(i, false);
		}
	}
}
