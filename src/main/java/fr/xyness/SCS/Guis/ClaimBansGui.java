package fr.xyness.SCS.Guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.lone.itemsadder.api.CustomStack;
import fr.xyness.SCS.ClaimMain;
import fr.xyness.SCS.SimpleClaimSystem;
import fr.xyness.SCS.Config.ClaimGuis;
import fr.xyness.SCS.Config.ClaimLanguage;
import fr.xyness.SCS.Config.ClaimSettings;
import me.clip.placeholderapi.PlaceholderAPI;

public class ClaimBansGui implements InventoryHolder {
	
	
	// ***************
	// *  Variables  *
	// ***************

	
    private Inventory inv;
    private static Map<Player,Map<Integer,String>> claimsBans = new HashMap<>();
    private static Map<Player,Chunk> chunks = new HashMap<>();
    
    
	// ******************
	// *  Constructors  *
	// ******************
    
    
    // Main constructor
    public ClaimBansGui(Player player, Chunk chunk, int page) {
    	String title = ClaimGuis.getGuiTitle("bans").replaceAll("%name%", ClaimMain.getClaimNameByChunk(chunk)).replaceAll("%page%", String.valueOf(page));
    	if(ClaimSettings.getBooleanSetting("placeholderapi")) {
    		title = PlaceholderAPI.setPlaceholders(player, title);
    	}
        inv = Bukkit.createInventory(this, ClaimGuis.getGuiRows("bans")*9, title);
        initializeItems(player,chunk,page);
    }
    
    
	// ********************
	// *  Others Methods  *
	// ********************
    
    
    // Method to set the chunk for a player
    public static void setChunk(Player player, Chunk chunk) {
    	chunks.put(player, chunk);
    }
    
    // Method to get the chunk for a player
    public static Chunk getChunk(Player player) {
    	if(chunks.containsKey(player)) {
    		return chunks.get(player);
    	}
    	return null;
    }
    
    // Method to remove the chunk for a player
    public static void removeChunk(Player player) {
    	if(chunks.containsKey(player)) {
    		chunks.remove(player);
    	}
    }
    
    // Method to get member (by slot) for a player
    public static String getClaimBan(Player player, int slot) {
    	return claimsBans.get(player).get(slot);
    }
    
    // Method to remove member for a player
    public static void removeClaimBan(Player player) {
    	if(claimsBans.containsKey(player)) {
    		claimsBans.remove(player);
    	}
    }

    // Method to initialize items for the gui
    public void initializeItems(Player player, Chunk chunk, int page) {
    	
    	if(SimpleClaimSystem.isFolia()) {
    		Bukkit.getAsyncScheduler().runNow(ClaimMain.getPlugin(), task -> {
    			chunks.put(player, chunk);
    	    	int min_member_slot = ClaimGuis.getGuiMinSlot("bans");
    	    	int max_member_slot = ClaimGuis.getGuiMaxSlot("bans");
    	    	int items_count = max_member_slot - min_member_slot + 1;
    	    	
    	        if(page > 1) {
    	        	inv.setItem(ClaimGuis.getItemSlot("bans", "back-page-list"), backPage(page-1));
    	        } else {
    	        	inv.setItem(ClaimGuis.getItemSlot("bans", "back-page-settings"), backPage2());
    	        }
    	        
    	        List<String> lore = new ArrayList<>();
    	        String owner = ClaimMain.getOwnerInClaim(chunk);
    	        if(owner.equals("admin")) {
    	        	lore = new ArrayList<>(getLore(ClaimLanguage.getMessage("player-banned-protected-area-lore")));
    	        } else {
    	        	lore = new ArrayList<>(getLore(ClaimLanguage.getMessage("player-banned-lore")));
    	        }
    	        Map<Integer,String> claims_bans = new HashMap<>();
    	        int startItem = (page - 1) * items_count;
    	    	int i = min_member_slot;
    	    	int count = 0;
    	        for(String p : ClaimMain.getClaimBans(chunk)) {
    	        	if (count++ < startItem) continue;
    	            if(i == max_member_slot+1) { 
    	            	inv.setItem(ClaimGuis.getItemSlot("bans", "next-page-list"), nextPage(page+1));
    	            	break;
    	            }
    	            List<String> lore2 = new ArrayList<>(getLoreWP(lore,p));
    	            claims_bans.put(i, p);
    	            if(ClaimGuis.getItemCheckCustomModelData("bans", "player-item")) {
    	            	inv.setItem(i, createItemWMD(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p),
    							lore2,
    							ClaimGuis.getItemMaterialMD("bans", "player-item"),
    							ClaimGuis.getItemCustomModelData("bans", "player-item")));
    	            	i++;
    	            	continue;
    	            }
    	        	if(ClaimGuis.getItemMaterialMD("bans", "player-item").contains("PLAYER_HEAD")) {
    	            	ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
    	    	        SkullMeta meta = (SkullMeta) item.getItemMeta();
    	                meta.setOwner(p);
    	                meta.setDisplayName(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p));
    	                meta.setLore(lore2);
    	                item.setItemMeta(meta);
    	                inv.setItem(i, item);
    	                i++;
    	                continue;
    	        	}
    	        	ItemStack item = new ItemStack(ClaimGuis.getItemMaterial("bans", "player-item"),1);
    	        	ItemMeta meta = item.getItemMeta();
    	        	meta.setDisplayName(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p));
    	            meta.setLore(lore2);
    	            item.setItemMeta(meta);
    	            inv.setItem(i, item);
    	            i++;
    	        }
    	        claimsBans.put(player, claims_bans);
    	        
    	    	Set<String> custom_items = new HashSet<>(ClaimGuis.getCustomItems("bans"));
    	    	for(String key : custom_items) {
    	    		lore = new ArrayList<>(getLoreP(ClaimGuis.getCustomItemLore("bans", key),player));
    	    		String title = ClaimGuis.getCustomItemTitle("bans", key);
    	    		if(ClaimSettings.getBooleanSetting("placeholderapi")) {
    	    			title = PlaceholderAPI.setPlaceholders(player, title);
    	    		}
    				if(ClaimGuis.getCustomItemCheckCustomModelData("bans", key)) {
    					inv.setItem(ClaimGuis.getCustomItemSlot("bans", key), createItemWMD(title,
    							lore,
    							ClaimGuis.getCustomItemMaterialMD("bans", key),
    							ClaimGuis.getCustomItemCustomModelData("bans", key)));
    				} else {
    					inv.setItem(ClaimGuis.getCustomItemSlot("bans", key), createItem(ClaimGuis.getCustomItemMaterial("bans", key),
    							title,
    							lore));
    				}
    	    	}
    		});
    	} else {
    		Bukkit.getScheduler().runTaskAsynchronously(ClaimMain.getPlugin(), task -> {
    			chunks.put(player, chunk);
    	    	int min_member_slot = ClaimGuis.getGuiMinSlot("bans");
    	    	int max_member_slot = ClaimGuis.getGuiMaxSlot("bans");
    	    	int items_count = max_member_slot - min_member_slot + 1;
    	    	
    	        if(page > 1) {
    	        	inv.setItem(ClaimGuis.getItemSlot("bans", "back-page-list"), backPage(page-1));
    	        } else {
    	        	inv.setItem(ClaimGuis.getItemSlot("bans", "back-page-settings"), backPage2());
    	        }
    	        
    	        List<String> lore = new ArrayList<>();
    	        String owner = ClaimMain.getOwnerInClaim(chunk);
    	        if(owner.equals("admin")) {
    	        	lore = new ArrayList<>(getLore(ClaimLanguage.getMessage("player-banned-protected-area-lore")));
    	        } else {
    	        	lore = new ArrayList<>(getLore(ClaimLanguage.getMessage("player-banned-lore")));
    	        }
    	        Map<Integer,String> claims_bans = new HashMap<>();
    	        int startItem = (page - 1) * items_count;
    	    	int i = min_member_slot;
    	    	int count = 0;
    	        for(String p : ClaimMain.getClaimBans(chunk)) {
    	        	if (count++ < startItem) continue;
    	            if(i == max_member_slot+1) { 
    	            	inv.setItem(ClaimGuis.getItemSlot("bans", "next-page-list"), nextPage(page+1));
    	            	break;
    	            }
    	            List<String> lore2 = new ArrayList<>(getLoreWP(lore,p));
    	            claims_bans.put(i, p);
    	            if(ClaimGuis.getItemCheckCustomModelData("bans", "player-item")) {
    	            	inv.setItem(i, createItemWMD(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p),
    							lore2,
    							ClaimGuis.getItemMaterialMD("bans", "player-item"),
    							ClaimGuis.getItemCustomModelData("bans", "player-item")));
    	            	i++;
    	            	continue;
    	            }
    	        	if(ClaimGuis.getItemMaterialMD("bans", "player-item").contains("PLAYER_HEAD")) {
    	            	ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
    	    	        SkullMeta meta = (SkullMeta) item.getItemMeta();
    	                meta.setOwner(p);
    	                meta.setDisplayName(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p));
    	                meta.setLore(lore2);
    	                item.setItemMeta(meta);
    	                inv.setItem(i, item);
    	                i++;
    	                continue;
    	        	}
    	        	ItemStack item = new ItemStack(ClaimGuis.getItemMaterial("bans", "player-item"),1);
    	        	ItemMeta meta = item.getItemMeta();
    	        	meta.setDisplayName(ClaimLanguage.getMessageWP("player-ban-title",p).replace("%player%", p));
    	            meta.setLore(lore2);
    	            item.setItemMeta(meta);
    	            inv.setItem(i, item);
    	            i++;
    	        }
    	        claimsBans.put(player, claims_bans);
    	        
    	    	Set<String> custom_items = new HashSet<>(ClaimGuis.getCustomItems("bans"));
    	    	for(String key : custom_items) {
    	    		lore = new ArrayList<>(getLoreP(ClaimGuis.getCustomItemLore("bans", key),player));
    	    		String title = ClaimGuis.getCustomItemTitle("bans", key);
    	    		if(ClaimSettings.getBooleanSetting("placeholderapi")) {
    	    			title = PlaceholderAPI.setPlaceholders(player, title);
    	    		}
    				if(ClaimGuis.getCustomItemCheckCustomModelData("bans", key)) {
    					inv.setItem(ClaimGuis.getCustomItemSlot("bans", key), createItemWMD(title,
    							lore,
    							ClaimGuis.getCustomItemMaterialMD("bans", key),
    							ClaimGuis.getCustomItemCustomModelData("bans", key)));
    				} else {
    					inv.setItem(ClaimGuis.getCustomItemSlot("bans", key), createItem(ClaimGuis.getCustomItemMaterial("bans", key),
    							title,
    							lore));
    				}
    	    	}
    		});
    	}

    }
    
    // Method to split lore for lines
    public static List<String> getLore(String lore){
    	List<String> lores = new ArrayList<>();
    	String[] parts = lore.split("\n");
    	for(String s : parts) {
    		lores.add(s);
    	}
    	return lores;
    }
    
    // Method to split the lore for lines with placeholders from PAPI
    public static List<String> getLoreP(String lore, Player player){
    	if(!ClaimSettings.getBooleanSetting("placeholderapi")) {
    		return getLore(lore);
    	}
    	List<String> lores = new ArrayList<>();
    	String[] parts = lore.split("\n");
    	for(String s : parts) {
    		lores.add(PlaceholderAPI.setPlaceholders(player, s));
    	}
    	return lores;
    }
    
    // Method to get the lore with placeholders from PAPI
    public static List<String> getLoreWP(List<String> lore, String player){
    	if(!ClaimSettings.getBooleanSetting("placeholderapi")) return lore;
    	List<String> lores = new ArrayList<>();
    	Player p = Bukkit.getPlayer(player);
    	if(p == null) {
    		OfflinePlayer o_offline = Bukkit.getOfflinePlayer(player);
        	for(String s : lore) {
        		lores.add(PlaceholderAPI.setPlaceholders(o_offline, s));
        	}
        	return lores;
    	}
    	for(String s : lore) {
    		lores.add(PlaceholderAPI.setPlaceholders(p, s));
    	}

    	return lores;
    }
    
    // Method to create item in the gui
    private ItemStack createItem(Material material, String name, List<String> lore) {
    	ItemStack item = null;
    	if(material == null) {
        	ClaimMain.getPlugin().getLogger().info("Error material loading, check members.yml");
        	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
        	item = new ItemStack(Material.STONE,1);
    	} else {
    		item = new ItemStack(material, 1);
    	}
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    // Method to create custom item in the gui
    private ItemStack createItemWMD(String name, List<String> lore, String name_custom_item, int model_data) {
        CustomStack customStack = CustomStack.getInstance(name_custom_item);
        ItemStack item = null;
        if(customStack == null) {
        	ClaimMain.getPlugin().getLogger().info("Error custom item loading : "+name_custom_item);
        	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
        	item = new ItemStack(Material.STONE,1);
        } else {
        	item = customStack.getItemStack();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.setCustomModelData(model_data);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    // Back page slot
    private ItemStack backPage(int page) {
    	ItemStack item = null;
    	if(ClaimGuis.getItemCheckCustomModelData("bans", "back-page-list")) {
    		CustomStack customStack = CustomStack.getInstance(ClaimGuis.getItemMaterialMD("bans", "back-page-list"));
            if(customStack == null) {
            	ClaimMain.getPlugin().getLogger().info("Error custom item loading : "+ClaimGuis.getItemMaterialMD("bans", "back-page-list"));
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
            	item = new ItemStack(Material.STONE,1);
            } else {
            	item = customStack.getItemStack();
            }
    	} else {
    		Material material = ClaimGuis.getItemMaterial("bans", "back-page-list");
    		if(material == null) {
            	ClaimMain.getPlugin().getLogger().info("Error material loading, check members.yml");
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
    			material = Material.STONE;
    		}
    		item = new ItemStack(material, 1);
    	}
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ClaimLanguage.getMessage("previous-page-title").replaceAll("%page%", String.valueOf(page)));
            meta.setLore(getLore(ClaimLanguage.getMessage("previous-page-lore").replaceAll("%page%", String.valueOf(page))));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }
    
    // Back page 2 slot
    private ItemStack backPage2() {
    	ItemStack item = null;
    	if(ClaimGuis.getItemCheckCustomModelData("bans", "back-page-settings")) {
    		CustomStack customStack = CustomStack.getInstance(ClaimGuis.getItemMaterialMD("bans", "back-page-settings"));
            if(customStack == null) {
            	ClaimMain.getPlugin().getLogger().info("Error custom item loading : "+ClaimGuis.getItemMaterialMD("bans", "back-page-settings"));
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
            	item = new ItemStack(Material.STONE,1);
            } else {
            	item = customStack.getItemStack();
            }
    	} else {
    		Material material = ClaimGuis.getItemMaterial("bans", "back-page-settings");
    		if(material == null) {
            	ClaimMain.getPlugin().getLogger().info("Error material loading, check members.yml");
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
    			material = Material.STONE;
    		}
    		item = new ItemStack(material, 1);
    	}
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ClaimLanguage.getMessage("previous-page-settings-title"));
            meta.setLore(getLore(ClaimLanguage.getMessage("previous-page-settings-lore")));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }
    
    // Next page slot
    private ItemStack nextPage(int page) {
    	ItemStack item = null;
    	if(ClaimGuis.getItemCheckCustomModelData("bans", "next-page-list")) {
    		CustomStack customStack = CustomStack.getInstance(ClaimGuis.getItemMaterialMD("bans", "next-page-list"));
            if(customStack == null) {
            	ClaimMain.getPlugin().getLogger().info("Error custom item loading : "+ClaimGuis.getItemMaterialMD("bans", "next-page-list"));
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
            	item = new ItemStack(Material.STONE,1);
            } else {
            	item = customStack.getItemStack();
            }
    	} else {
    		Material material = ClaimGuis.getItemMaterial("bans", "next-page-list");
    		if(material == null) {
            	ClaimMain.getPlugin().getLogger().info("Error material loading, check members.yml");
            	ClaimMain.getPlugin().getLogger().info("Using STONE instead");
    			material = Material.STONE;
    		}
    		item = new ItemStack(material, 1);
    	}
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ClaimLanguage.getMessage("next-page-title").replaceAll("%page%", String.valueOf(page)));
            meta.setLore(getLore(ClaimLanguage.getMessage("next-page-lore").replaceAll("%page%", String.valueOf(page))));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }
    
    @Override
    public Inventory getInventory() {
        return inv;
    }
    
    public void openInventory(Player player) {
        player.openInventory(inv);
    }

}
