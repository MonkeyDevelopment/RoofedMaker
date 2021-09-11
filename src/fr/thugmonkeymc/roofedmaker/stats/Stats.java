package fr.thugmonkeymc.roofedmaker.stats;

import net.md_5.bungee.api.ChatColor;

public class Stats {
	private int chunkCleared = 0;
	private int chunkPlanted = 0;
	private int treesPlanted = 0;
	private long startTime = 0;
	private long endTime = 0;
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public int getSecondsElapsed() {
		return endTime == 0 ? Math.round((System.currentTimeMillis() - startTime) / 1000) : Math.round((endTime - startTime) / 1000);
	}
	
	public int getTreesPlanted() {
		return treesPlanted;
	}
	
	public void setTreesPlanted(int treesPlanted) {
		this.treesPlanted = treesPlanted;
	}
	
	public int getChunkPlanted() {
		return chunkPlanted;
	}
	
	public void setChunkPlanted(int chunkPlanted) {
		this.chunkPlanted = chunkPlanted;
	}
	
	public int getChunkCleared() {
		return chunkCleared;
	}
	
	public void setChunkCleared(int chunkCleared) {
		this.chunkCleared = chunkCleared;
	}
	
	public void increaseChunksCleared() {
		this.chunkCleared++;
	}
	
	public void increaseChunksPlanted() {
		this.chunkPlanted++;
	}
	
	public void increaseTreesPlanted() {
		this.treesPlanted++;
	}
	
	public String getTimePassed() {
		return ChatColor.GOLD.toString() + (int) Math.floor(getSecondsElapsed()/60) + ChatColor.AQUA + "m" + 
	ChatColor.GOLD.toString() + (getSecondsElapsed() > 60 ? getSecondsElapsed() % 60 : getSecondsElapsed()) + ChatColor.AQUA + "s";
	}
}
