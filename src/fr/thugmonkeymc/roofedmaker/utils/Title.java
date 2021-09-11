package fr.thugmonkeymc.roofedmaker.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class Title {

	    private static final Map<String, EntityEnderDragon> dragons;
	    
	    @SuppressWarnings("rawtypes")
		public static void sendTitle(final Player player, final Integer fadeIn, final Integer stay, final Integer fadeOut, String title, String subtitle) {
	        final PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
	        final PacketPlayOutTitle packetPlayOutTimes = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, (IChatBaseComponent)null, (int)fadeIn, (int)stay, (int)fadeOut);
	        connection.sendPacket((Packet)packetPlayOutTimes);
	        if (subtitle != null) {
	            subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
	            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
	            final IChatBaseComponent titleSub = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
	            final PacketPlayOutTitle packetPlayOutSubTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, titleSub);
	            connection.sendPacket((Packet)packetPlayOutSubTitle);
	        }
	        if (title != null) {
	            title = title.replaceAll("%player%", player.getDisplayName());
	            title = ChatColor.translateAlternateColorCodes('&', title);
	            final IChatBaseComponent titleMain = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
	            final PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleMain);
	            connection.sendPacket((Packet)packetPlayOutTitle);
	        }
	    }
	    
	    @SuppressWarnings("rawtypes")
		public static void sendTabTitle(final Player player, String header, String footer) {
	        
	        if (header == null) {
	            header = "";
	        }
	        header = ChatColor.translateAlternateColorCodes('&', header);
	        if (footer == null) {
	            footer = "";
	        }
	        footer = ChatColor.translateAlternateColorCodes('&', footer);
	        header = header.replaceAll("%player%", player.getDisplayName());
	        footer = footer.replaceAll("%player%", player.getDisplayName());
	        final PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
	        final IChatBaseComponent tabTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
	        final IChatBaseComponent tabFoot = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");
	        final PacketPlayOutPlayerListHeaderFooter headerPacket = new PacketPlayOutPlayerListHeaderFooter(tabTitle);
	        try {
	            final Field field = headerPacket.getClass().getDeclaredField("b");
	            field.setAccessible(true);
	            field.set(headerPacket, tabFoot);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	        finally {
	            connection.sendPacket((Packet)headerPacket);
	        }
	    }
	    
	    @SuppressWarnings("rawtypes")
		public static void sendActionBar(final Player player, final String message) {
	        final CraftPlayer p = (CraftPlayer)player;
	        final IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
	        p.getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutChat(cbc, (byte)2));
	    }
	    
	    public static void sendActionBar(final String message) {
	    	for (Player player : Bukkit.getOnlinePlayers()) {
	    		sendActionBar(player, message);
			}
	    }
	    
	    @SuppressWarnings("rawtypes")
		public static void removeBar(final Player p) {
	        if (Title.dragons.containsKey(p.getName())) {
	            final PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { Title.dragons.get(p.getName()).getId() });
	            Title.dragons.remove(p.getName());
	            ((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet)packet);
	        }
	    }
	    
	    static {
	        dragons = new ConcurrentHashMap<String, EntityEnderDragon>();
	    }
	}


