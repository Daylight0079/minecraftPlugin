package com.Daylight;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class lockchestListener implements Listener{
	
    private final lockchest plugin;
    public lockchestListener(lockchest plugin) {
        this.plugin = plugin;
    }
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if (event.getLine(0).equalsIgnoreCase("[private]")) {
            event.setLine(1, player.getName());
            String location = event.getBlock().getLocation().toString();
            plugin.getLockedChests().put(location, player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Chest locked successfully!");
        }
    }
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.CHEST) {
                Block signBlock = getAttachedSign(block);
                if (signBlock != null && signBlock.getState() instanceof Sign) {
                    Sign sign = (Sign) signBlock.getState();
                    if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]")) {
                        Player player = event.getPlayer();
                        String location = signBlock.getLocation().toString();
                        UUID ownerUUID = plugin.getLockedChests().get(location);
                        if (ownerUUID != null && !ownerUUID.equals(player.getUniqueId())) {
                            player.sendMessage(ChatColor.RED + "This chest is locked!");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST) {
            Block signBlock = getAttachedSign(block);
            if (signBlock != null && signBlock.getState() instanceof Sign) {
                Sign sign = (Sign) signBlock.getState();
                if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]")) {
                    Player player = event.getPlayer();
                    String location = signBlock.getLocation().toString();
                    UUID ownerUUID = plugin.getLockedChests().get(location);
                    if (ownerUUID != null && !ownerUUID.equals(player.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + "You cannot break this chest!");
                        event.setCancelled(true);
                    } else {
                        plugin.getLockedChests().remove(location); // 잠금 해제
                    }
                }
            }
        } else if (block.getBlockData() instanceof WallSign) {
            Sign sign = (Sign) block.getState();
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]")) {
                Player player = event.getPlayer();
                String location = block.getLocation().toString();
                UUID ownerUUID = plugin.getLockedChests().get(location);
                if (ownerUUID != null && !ownerUUID.equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You cannot break this sign!");
                    event.setCancelled(true);
                } else {
                    plugin.getLockedChests().remove(location); // 잠금 해제
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        if (isProtectedBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isProtectedBlock);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isProtectedBlock);
    }

    private boolean isProtectedBlock(Block block) {
        if (block.getType() == Material.CHEST || block.getType() == Material.OAK_WALL_SIGN) {
            Block signBlock = getAttachedSign(block);
            if (signBlock != null && signBlock.getState() instanceof Sign) {
                Sign sign = (Sign) signBlock.getState();
                return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]");
            }
        }
        if (block.getType() == Material.OAK_WALL_SIGN) {
            Sign sign = (Sign) block.getState();
            return ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[private]");
        }
        return false;
    }

    private Block getAttachedSign(Block chest) {
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block relative = chest.getRelative(face);
            if (relative.getType() == Material.OAK_WALL_SIGN) {
                WallSign wallSign = (WallSign) relative.getBlockData();
                if (relative.getRelative(wallSign.getFacing().getOppositeFace()).equals(chest)) {
                    return relative;
                }
            }
        }
        return null;
    }
	
}
