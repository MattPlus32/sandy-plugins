package net.runelite.client.plugins.agilitypyramid;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;

import static net.runelite.api.MenuOpcode.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

@Slf4j
@Singleton
class AgilityPyramidOverlay extends OverlayPanel
{
	private final Client client;
	private final AgilityPyramidPlugin plugin;
	private final AgilityPyramidConfig config;

	String timeFormat;
	private String infoStatus = "Starting...";

	@Inject
	private AgilityPyramidOverlay(final Client client, final AgilityPyramidPlugin plugin, final AgilityPyramidConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Agility Pyramid overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.botTimer == null || !plugin.startAgility || !config.enableUI())
		{
			log.debug("Overlay conditions not met, not starting overlay");
			return null;
		}
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		Duration duration = Duration.between(plugin.botTimer, Instant.now());
		timeFormat = (duration.toHours() < 1) ? "mm:ss" : "HH:mm:ss";
		tableComponent.addRow("Time running:", formatDuration(duration.toMillis(), timeFormat));
		if (plugin.state != null)
		{
			if (!plugin.state.name().equals("TIMEOUT"))
			{
				infoStatus = plugin.state.name();
			}
		}
		tableComponent.addRow("Status:", infoStatus);
		if (config.humidify()) {
			if (plugin.waterskinsLeft != -1) {
				tableComponent.addRow("Water left:", plugin.waterskinsLeft + " sips");
			}
		}

		TableComponent tableTopsComponent = new TableComponent();
		tableTopsComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		TableComponent tableDelayComponent = new TableComponent();
		tableDelayComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableDelayComponent.addRow("Sleep delay:", plugin.sleepLength + "ms");
		tableDelayComponent.addRow("Tick delay:", String.valueOf(plugin.timeout));

		if (!tableComponent.isEmpty())
		{
			panelComponent.setBackgroundColor(ColorUtil.fromHex("#121212")); //Material Dark default
			panelComponent.setPreferredSize(new Dimension(200, 200));
			panelComponent.setBorder(new Rectangle(5, 5, 5, 5));
			panelComponent.getChildren().add(TitleComponent.builder()
					.text("Sandy Agility Pyramid")
					.color(ColorUtil.fromHex("#ffbf00"))
					.build());
			panelComponent.getChildren().add(tableComponent);
			panelComponent.getChildren().add(TitleComponent.builder()
					.text("Delays")
					.color(ColorUtil.fromHex("#ffbf00"))
					.build());
			panelComponent.getChildren().add(tableDelayComponent);
		}
		return super.render(graphics);
	}
}