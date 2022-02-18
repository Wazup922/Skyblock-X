package me.wazup.skyblock.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

	public static Method setDamageMethod;

	private ItemStack item;
	private ItemMeta meta;
	private List<String> lore;
	
	public ItemStackBuilder(ItemStack item){
	this.item = item;
	meta = item.getItemMeta();
	lore = meta != null && meta.hasLore() ? meta.getLore() : new ArrayList<>();
	}
	
	public ItemStackBuilder(Material material){
	this(new ItemStack(material));
	}
	
	public ItemStackBuilder setType(Material type){ item.setType(type); return this;}
	public ItemStackBuilder setName(String name){ meta.setDisplayName(name); return this; }
	public ItemStackBuilder addLore(String... l){ for(String x: l) lore.add(x); return this; }
	public ItemStackBuilder addEnchantment(Enchantment e, int level){
		if(item.getType().equals(Material.ENCHANTED_BOOK)){
			((EnchantmentStorageMeta) meta).addStoredEnchant(e, level, true);
		} else meta.addEnchant(e, level, true);
		return this;
	}
	public ItemStackBuilder setAmount(int amount){ item.setAmount(amount); return this;}
	
	public ItemStackBuilder setDurability(int durability){
	if(setDamageMethod != null) {
	try {
	setDamageMethod.invoke(meta, (short) durability);
	} catch (Exception e) { e.printStackTrace(); }
	} else item.setDurability((short) durability);
	return this; }
	
	public void setPotionEffect(PotionType type, boolean extend, boolean upgrade) {
	PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
	try {
	PotionMeta.class.getMethod("setBasePotionData", Class.forName("org.bukkit.potion.PotionData")).invoke(potionMeta, Class.forName("org.bukkit.potion.PotionData").getConstructor(PotionType.class, boolean.class, boolean.class).newInstance(type, extend, upgrade));
	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | InstantiationException e) { Bukkit.getConsoleSender().sendMessage("[Skywars] The used format for potions does not work on this minecraft version, please try other format which is POTION:DURABILITY");}
	meta = potionMeta;
	}
	
	public ItemStack build(){
	if(!lore.isEmpty()){
	meta.setLore(lore);
	lore.clear();
	}
	item.setItemMeta(meta);
	return item;
	}

	public static void loadMethods(){
		try {
			for(Method m: Class.forName("org.bukkit.inventory.meta.Damageable").getMethods()) {
				if(m.getName().equals("setDamage")) {
					setDamageMethod = m;
					break;
				}
			}
		} catch (Exception e) {}
	}

}
