package com.khjxiaogu.ItemDataBase.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemGivenEvent extends Event implements Cancellable {
	protected Player player;
	protected ItemStack item;
	private static final HandlerList handlers = new HandlerList();
	protected boolean cancelled=false;
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public ItemGivenEvent(Player p, ItemStack is) {
		this.player = p;
		this.item = is;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled=cancel;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
