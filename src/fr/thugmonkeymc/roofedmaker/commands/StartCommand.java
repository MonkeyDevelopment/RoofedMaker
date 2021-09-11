package fr.thugmonkeymc.roofedmaker.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import fr.thugmonkeymc.roofedmaker.main.Main;
import fr.thugmonkeymc.roofedmaker.stats.Stats;
import fr.thugmonkeymc.roofedmaker.utils.Title;
import net.md_5.bungee.api.ChatColor;

public class StartCommand implements CommandExecutor {
	
	private static String WORLD_NAME = "world";
	private final List<List<Integer>> integersList = new ArrayList<List<Integer>>();
	private final List<Chunk> chunks = new ArrayList<Chunk>();
	private final List<Chunk> treesChunks = new ArrayList<Chunk>();
	private final Main main;
	private int radius = 200;
	private int totalChunks = 0;
	private Stats stats = new Stats();
	private Player player;
	private State state = State.NOT_STARTED;
	private int toClear = 296;
	
	public StartCommand(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		System.out.println("Starting map clear...");
		if(args.length == 1) {
			radius = Integer.parseInt(args[0]);
			System.out.println("radius is now " + radius);
			toClear = radius + 50;
		}
		stats.setStartTime(System.currentTimeMillis());
		this.state = State.STARTING;
		clearMap();
		StartCommand.WORLD_NAME = this.main.getConfig().getString("world_name");
		
		if(sender instanceof Player) {
			this.player = (Player) sender;
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(state == State.ENDED) {
					cancel();
				}
				notifyPlayer();
			}
		}.runTaskTimer(this.main, 0, 1);
		
		return true;
	}
	
	private void clearMap() {
		System.out.println("Stocking chunks start");
		this.state = State.COUNTING;
		for(int x = -toClear; x < toClear; x+=16) {
			for(int z = -toClear; z < toClear; z+=16) {
				System.out.println("  added x:" + x + " z:" + z);
				integersList.add(Arrays.asList(x, z));
				totalChunks = integersList.size();
			}
		}
		System.out.println("Stocked chunks");
		System.out.println("Loading all chunks...");
		convertChunks();
	}
	
	private void convertChunks() {
		this.state = State.INDEXATION;
		int chunks_perTick = this.main.getConfig().getInt("generation.chunks_clears_per_tick");
		new BukkitRunnable() {
			@Override
			public void run() {
				for(int i = 0; i < chunks_perTick; i++) {	
					if(integersList.isEmpty() || integersList.size() == 0) {
						startClearChunks();
						cancel();
						return;
					}			
					
					Chunk chunk = Bukkit.getWorld(WORLD_NAME).getChunkAt(integersList.get(0).get(0), integersList.get(0).get(1));
					System.out.println("  loading chunk x:" + chunk.getX() + " z:" + chunk.getZ());
					integersList.remove(0);
					chunks.add(chunk);
				}
			}
		}.runTaskTimer(main, 0, 1);
		
		System.out.println("Loaded all chunks");
	}
	
	private void startClearChunks() {
		System.out.println("Started map clear !");
		this.state = State.CLEARING;
		Bukkit.getWorld(WORLD_NAME).setGameRuleValue("randomTickSpeed", "500");
		Bukkit.getWorld(WORLD_NAME).setGameRuleValue("doFireTick", "false");
		int chunk_perTick = this.main.getConfig().getInt("generation.chunks_clears_per_tick");
		new BukkitRunnable() {
			@Override
			public void run() {
				for(int i = 0; i < chunk_perTick; i++) {	
					if(chunks.isEmpty() || chunks.size() == 0) {
						System.out.println("no more chunks so aborting");
						System.out.println("will soon start trees planting");
						new BukkitRunnable() {
							@Override
							public void run() {
								plantTrees();
							}
						}.runTaskLater(main, 40);
						cancel();
						return;
					}
					clearChunk(chunks.get(i));
					treesChunks.add(chunks.get(i));
					chunks.remove(i);
				}
			}
		}.runTaskTimer(StartCommand.this.main, 10, 1);
	}
	
	private void clearChunk(Chunk chunk) {
		System.out.println("Clearing chunk x:" + chunk.getX() + " z:" + chunk.getZ() + "...");
		player.teleport(new Location(Bukkit.getWorld(WORLD_NAME), chunk.getX(), 100, chunk.getZ()), TeleportCause.PLUGIN);
		chunk.load(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {
						clearXZ(chunk, chunk.getX() + x, chunk.getZ() + z);
					}
				}
				
			}
		}.runTaskLater(main, 80);
		chunk.unload();
		stats.increaseChunksCleared();
	}
	
	private void clearXZ(Chunk chunk, int x, int z) {
		System.out.println("    -> Clearing x:" + x + " z:" + z);
		List<Material> blockToStopOn = Arrays.asList(Material.STONE,
				Material.DIRT,
				Material.GRASS,
				Material.GRAVEL,
				Material.SAND,
				Material.SANDSTONE,
				Material.IRON_ORE,
				Material.COAL_ORE,
				Material.CLAY,
				Material.WATER,
				Material.LAVA,
				Material.STATIONARY_LAVA,
				Material.STATIONARY_WATER);
		
		for(int y = Bukkit.getWorld(WORLD_NAME).getHighestBlockYAt(x, z) + 2; y > 55; y--) {
			Block block = Bukkit.getWorld(WORLD_NAME).getBlockAt(x, y, z);
			if(blockToStopOn.contains(block.getType())) {
				System.out.println("stopped at the y:" + y + " because of a block to stop on was found");
				return;
			} else {
				System.out.println("cleared x:" + x + " y:" + y + " z:" + z);
				block.setType(Material.AIR);
			}
		}
	}
	
	private void plantTrees() {
		System.out.println("Starting trees plant");
		player.teleport(new Location(Bukkit.getWorld(WORLD_NAME), 0, 100, 0));
		player.setGameMode(GameMode.SPECTATOR);
		startPlantChunks();
	}
	
	private void startPlantChunks() {
		this.state = State.TREES;
		int chunk_perTick = this.main.getConfig().getInt("generation.chunks_clears_per_tick");
		new BukkitRunnable() {
			@Override
			public void run() {
				for(int i = 0; i < chunk_perTick; i++) {	
					if(treesChunks.isEmpty() || treesChunks.size() == 0) {
						System.out.println("no more chunks to plant trees on so aborting");
						Bukkit.getWorld(WORLD_NAME).setGameRuleValue("randomTickSpeed", "3");
						stats.setEndTime(System.currentTimeMillis());
						for (Player opplayer : Bukkit.getOnlinePlayers()) {
							if(opplayer.isOp()) {
								player.sendMessage(ChatColor.GREEN + "\n--------------------------"
										+ "\nLa roofed s'est bien générée," + ChatColor.YELLOW + " il est important de vérifier que la lave"
										+ "\nne soit pas trop proche des arbres pour que cela ne les crame pas." + ChatColor.BLUE
										+ "\nAprès avoir vérifier ceci n'oubliez pas de réactiver le firetick.\n\n" + ChatColor.GRAY + "Statistiques:"
										+ "\n" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + StartCommand.this.stats.getChunkCleared() + ChatColor.GREEN + " Chunks nettoyés"
										+ "\n" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + StartCommand.this.stats.getTreesPlanted() + ChatColor.GREEN + " Arbres plantés"
										+ "\n" + ChatColor.AQUA + " en seulement " + StartCommand.this.stats.getTimePassed());
							}
							
						}
						StartCommand.this.state = State.ENDED;
						cancel();
						return;
					}
					plantChunk(treesChunks.get(0));
					treesChunks.remove(0);
				}
			}
		}.runTaskTimer(StartCommand.this.main, 10, 1);
	}
	
	private void plantChunk(Chunk chunk) {		
		chunk.load(true);
		boolean chunkplanted = false;
		System.out.println("-> planting chunk x:" + chunk.getX() + " z:" + chunk.getZ());
		for(int i = 0; i < 25; i++) {
			int random = (int) Math.floor(Math.random() * 256);
			int x = random / 16;
			int z = random % 16;
			if(new Location(Bukkit.getWorld(WORLD_NAME), 0, 100, 0).distance(new Location(Bukkit.getWorld(WORLD_NAME), chunk.getX() + x, 100, chunk.getZ() + z)) < radius) {
				plantXZ(chunk, chunk.getX() + x, chunk.getZ() + z);
				chunkplanted = true;
			}
		}		
		
		if(chunkplanted) {
			stats.increaseChunksPlanted();
		}
		chunk.unload();
		System.out.println(" chunk planted !");
	}
	
	private void plantXZ(Chunk chunk, int x, int z) {
		System.out.println("   -> planting tree at x:" + x + " z:" + z);
		if(Bukkit.getWorld(WORLD_NAME).generateTree(
				new Location(Bukkit.getWorld(WORLD_NAME),
						x, Bukkit.getWorld(WORLD_NAME).getHighestBlockYAt(x, z), z),
				TreeType.DARK_OAK)) {
			stats.increaseTreesPlanted();
		}
		System.out.println("   -> tree has been planted");
	}
	
	private void notifyPlayer() {
		String message = "";
		switch(this.state) {
			case NOT_STARTED:
				message = ChatColor.BLUE + "Démarrage en cours...";
				break;
			case CLEARING:
				message = ChatColor.AQUA + "Nettoyage en cours... " + this.stats.getChunkCleared()  + "/" + totalChunks + " ≈" + Math.round((double)this.stats.getChunkCleared() / (double)totalChunks * 100.0) + "%";
				break;
			case TREES:
				message = ChatColor.AQUA + "Forestation en cours... Arbres plantés: " + this.stats.getTreesPlanted();
				break;
			case COUNTING:
				message = ChatColor.AQUA + "Décompte des chunks en cours... Chunks comptés: " + this.integersList.size();
				break;
			case INDEXATION:
				message = ChatColor.AQUA + "Indexation des chunks en cours... " + chunks.size()  + "/" + totalChunks + " ≈" + Math.round((double)chunks.size() / (double) totalChunks * 100.0) + "%";
				break;
			case ENDED:
				message = ChatColor.GREEN + "La tâche est finie et a prise " + this.stats.getTimePassed();
				break;
			case STARTING:
				message = ChatColor.AQUA + "Le nettoyage va démarrer...";
				break;
			default:
				break;
		}
		//▌
		for(Player playerO : Bukkit.getOnlinePlayers()) {
			if(playerO.isOp()) {				
				Title.sendActionBar(playerO, ChatColor.AQUA + "Temps passé: " + this.stats.getTimePassed() + "; " + message);
			}
		}
	}
	
	private enum State {
		INDEXATION,
		NOT_STARTED,
		STARTING,
		TREES,
		ENDED,
		CLEARING,
		COUNTING,
		;
	}
}