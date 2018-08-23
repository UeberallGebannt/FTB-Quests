package com.feed_the_beast.ftbquests.gui;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestFile;
import com.feed_the_beast.ftbquests.tile.TileQuestChest;
import com.feed_the_beast.ftbquests.util.PlayerRewards;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * @author LatvianModder
 */
public class ContainerQuestChest extends Container
{
	private class SlotInput extends SlotItemHandler
	{
		public SlotInput(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return true;
		}

		@Override
		public void putStack(ItemStack stack)
		{
		}

		@Override
		public int getSlotStackLimit()
		{
			return 64;
		}
	}

	private class SlotOutput extends SlotItemHandler
	{
		public SlotOutput(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return false;
		}

		@Override
		public void putStack(ItemStack stack)
		{
			rewards.setStackInSlot(getSlotIndex(), stack.isEmpty() ? ItemStack.EMPTY : getStack().copy());
		}

		@Override
		public void onSlotChanged()
		{
			putStack(getStack());
		}
	}

	public final EntityPlayer player;
	public final QuestFile questFile;
	public final PlayerRewards rewards;
	public final TileQuestChest chest;

	public ContainerQuestChest(EntityPlayer ep, TileQuestChest c)
	{
		player = ep;
		questFile = FTBQuests.PROXY.getQuestFile(player.world);
		rewards = questFile.getRewards(player);
		chest = c;

		int invX = 8;
		int invY = 107;

		addSlotToContainer(new SlotInput(c, 0, 8, 84));

		for (int i = 0; i < 6; i++)
		{
			addSlotToContainer(new SlotOutput(rewards, i, 44 + i * 18, 84));
		}

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, invX + x * 18, invY + y * 18));
			}
		}

		for (int x = 0; x < 9; x++)
		{
			addSlotToContainer(new Slot(player.inventory, x, invX + x * 18, invY + 58));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return !chest.isInvalid();
	}

	@Override
	public ItemStack slotClick(int slot, int button, ClickType type, EntityPlayer player)
	{
		if (slot == 0 && (button == 0 || button == 1) && (type == ClickType.PICKUP || type == ClickType.QUICK_CRAFT))
		{
			if (!player.inventory.getItemStack().isEmpty())
			{
				player.inventory.setItemStack(chest.insert(player.inventory.getItemStack(), false, player));
				player.inventory.markDirty();
			}

			return ItemStack.EMPTY;
		}
		else
		{
			return super.slotClick(slot, button, type, player);
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index)
	{
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			if (slot instanceof SlotInput)
			{
				return ItemStack.EMPTY;
			}
			else if (slot instanceof SlotOutput)
			{
				ItemStack stack = slot.getStack();
				ItemStack prevStack = stack.copy();

				if (!mergeItemStack(stack, 7, 36 + 7, true))
				{
					return ItemStack.EMPTY;
				}

				slot.onSlotChanged();
				return prevStack;
			}
			else
			{
				slot.putStack(chest.insert(slot.getStack(), false, player));
				return ItemStack.EMPTY;
			}
		}

		return ItemStack.EMPTY;
	}
}