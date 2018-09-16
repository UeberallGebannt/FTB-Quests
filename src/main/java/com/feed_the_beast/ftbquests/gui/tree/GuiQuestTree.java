package com.feed_the_beast.ftbquests.gui.tree;

import com.feed_the_beast.ftblib.lib.config.ConfigString;
import com.feed_the_beast.ftblib.lib.gui.ContextMenuItem;
import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.GuiHelper;
import com.feed_the_beast.ftblib.lib.gui.GuiIcons;
import com.feed_the_beast.ftblib.lib.gui.Panel;
import com.feed_the_beast.ftblib.lib.gui.Theme;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import com.feed_the_beast.ftblib.lib.gui.misc.GuiEditConfigValue;
import com.feed_the_beast.ftblib.lib.icon.Color4I;
import com.feed_the_beast.ftbquests.client.ClientQuestFile;
import com.feed_the_beast.ftbquests.gui.GuiVariables;
import com.feed_the_beast.ftbquests.gui.QuestsTheme;
import com.feed_the_beast.ftbquests.net.MessageCompleteInstantly;
import com.feed_the_beast.ftbquests.net.MessageResetProgress;
import com.feed_the_beast.ftbquests.net.edit.MessageChangeID;
import com.feed_the_beast.ftbquests.net.edit.MessageEditObject;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestChapter;
import com.feed_the_beast.ftbquests.quest.QuestObject;
import com.feed_the_beast.ftbquests.quest.QuestVariable;
import com.feed_the_beast.ftbquests.quest.tasks.QuestTask;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

public class GuiQuestTree extends GuiBase
{
	public final ClientQuestFile questFile;
	public int scrollWidth, scrollHeight, prevMouseX, prevMouseY, grabbed;
	public QuestChapter selectedChapter;
	public Quest selectedQuest;
	public final Panel chapterPanel, quests, questLeft, questRight, otherButtons;
	public Color4I borderColor, backgroundColor;
	public boolean movingQuest = false;
	public int zoom = 24;

	public GuiQuestTree(ClientQuestFile q)
	{
		questFile = q;

		chapterPanel = new PanelChapters(this);
		chapterPanel.setHeight(20);

		selectedChapter = questFile.chapters.isEmpty() ? null : questFile.chapters.get(0);
		borderColor = Color4I.WHITE.withAlpha(88);
		backgroundColor = Color4I.WHITE.withAlpha(33);

		quests = new PanelQuests(this);
		questLeft = new PanelQuestLeft(this);
		questRight = new PanelQuestRight(this);
		otherButtons = new PanelOtherButtons(this);

		selectChapter(null);
	}

	@Override
	public void addWidgets()
	{
		add(chapterPanel);
		add(quests);
		add(otherButtons);
		add(questLeft);
		add(questRight);
	}

	@Override
	public void alignWidgets()
	{
		otherButtons.setX(width - otherButtons.width - 1);
		chapterPanel.alignWidgets();
	}

	@Override
	public boolean onInit()
	{
		return setFullscreen();
	}

	public void selectChapter(@Nullable QuestChapter chapter)
	{
		if (selectedChapter != chapter)
		{
			movingQuest = false;
			selectQuest(null);
			selectedChapter = chapter;
			quests.setScrollX(0);
			quests.setScrollY(0);
			quests.refreshWidgets();
			resetScroll(true);
		}
	}

	public void selectQuest(@Nullable Quest quest)
	{
		if (selectedQuest != quest)
		{
			selectedQuest = quest;
			questLeft.refreshWidgets();
			questRight.refreshWidgets();
		}
	}

	public void resetScroll(boolean realign)
	{
		if (realign)
		{
			quests.alignWidgets();
		}

		quests.setScrollX((scrollWidth - quests.width) / 2);
		quests.setScrollY((scrollHeight - quests.height) / 2);
	}

	public void addObjectMenuItems(List<ContextMenuItem> contextMenu, GuiBase prevGui, QuestObject object)
	{
		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.edit"), GuiIcons.SETTINGS, () -> new MessageEditObject(object.getID()).sendToServer()));
		contextMenu.add(new ContextMenuItem(I18n.format("selectServer.delete"), GuiIcons.REMOVE, () -> questFile.deleteObject(object.getID())).setYesNo(I18n.format("delete_item", object.getDisplayName().getFormattedText())));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.reset_progress"), GuiIcons.REFRESH, () -> new MessageResetProgress(object.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.reset_progress_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.complete_instantly"), QuestsTheme.COMPLETED, () -> new MessageCompleteInstantly(object.getID()).sendToServer()).setYesNo(I18n.format("ftbquests.gui.complete_instantly_q")));
		contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.change_id"), GuiIcons.NOTES, () -> new GuiEditConfigValue("id", new ConfigString(object.id, QuestObject.ID_PATTERN), (value, set) -> {
			prevGui.openGui();

			if (set)
			{
				new MessageChangeID(object.getID(), value.getString()).sendToServer();
			}
		}).openGui()));

		//contextMenu.add(new ContextMenuItem(I18n.format("ftbquests.gui.copy_id"), GuiIcons.INFO, () -> setClipboardString(object.getID())));
	}

	@Override
	public boolean keyPressed(int key, char keyChar)
	{
		if (super.keyPressed(key, keyChar))
		{
			return true;
		}
		else if (key == Keyboard.KEY_TAB)
		{
			if (selectedChapter != null && !questFile.chapters.isEmpty())
			{
				selectChapter(questFile.chapters.get((selectedChapter.chapterIndex + 1) % questFile.chapters.size()));
				return true;
			}
		}
		else if (keyChar >= '1' && keyChar <= '9')
		{
			int i = keyChar - '1';

			if (i < questFile.chapters.size())
			{
				selectChapter(questFile.chapters.get(i));
				return true;
			}
		}
		else if (selectedChapter != null && questFile.canEdit() && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown())
		{
			switch (key)
			{
				case Keyboard.KEY_D:
					movingQuest = false;
					selectQuest(null);
					return true;
			}
		}

		return false;
	}

	@Override
	public void drawBackground(Theme theme, int x, int y, int w, int h)
	{
		if (selectedChapter != null && selectedChapter.invalid)
		{
			selectChapter(null);
		}

		if (selectedChapter == null && !questFile.chapters.isEmpty())
		{
			selectChapter(questFile.chapters.get(0));
		}

		super.drawBackground(theme, x, y, w, h);

		if (grabbed != 0)
		{
			int mx = getMouseX();
			int my = getMouseY();

			if (scrollWidth > quests.width)
			{
				quests.setScrollX(Math.max(Math.min(quests.getScrollX() + (prevMouseX - mx), scrollWidth - quests.width), 0));
			}
			else
			{
				quests.setScrollX((scrollWidth - quests.width) / 2);
			}

			if (scrollHeight > quests.height)
			{
				quests.setScrollY(Math.max(Math.min(quests.getScrollY() + (prevMouseY - my), scrollHeight - quests.height), 0));
			}
			else
			{
				quests.setScrollY((scrollHeight - quests.height) / 2);
			}

			prevMouseX = mx;
			prevMouseY = my;
		}
	}

	@Override
	public void drawForeground(Theme theme, int x, int y, int w, int h)
	{
		GuiHelper.drawHollowRect(x, y, w, h, borderColor, false);

		int start = 1;

		if (!chapterPanel.widgets.isEmpty())
		{
			Widget last = chapterPanel.widgets.get(chapterPanel.widgets.size() - 1);
			start = last.getX() + last.width + 1;
		}

		backgroundColor.draw(start, y + 1, w - start - otherButtons.width - 1, chapterPanel.height - 2);
		borderColor.draw(start, y + chapterPanel.height - 1, w - start - 1, 1);

		super.drawForeground(theme, x, y, w, h);
	}

	@Override
	public Theme getTheme()
	{
		return QuestsTheme.INSTANCE;
	}

	@Override
	public boolean drawDefaultBackground()
	{
		return false;
	}

	public void open(@Nullable QuestObject object)
	{
		if (object instanceof QuestVariable)
		{
			new GuiVariables().openGui();
			return;
		}
		else if (object instanceof QuestChapter)
		{
			selectChapter((QuestChapter) object);
		}
		else if (object instanceof Quest)
		{
			selectChapter(((Quest) object).chapter);
			selectQuest((Quest) object);
		}
		else if (object instanceof QuestTask)
		{
			selectChapter(((QuestTask) object).quest.chapter);
			selectQuest(((QuestTask) object).quest);
		}

		openGui();
	}
}