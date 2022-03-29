package me.wazup.skyblock.utils;

import me.wazup.skyblock.managers.Customization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlexibleSmartInventory {
	
    private String mainName;
	private HashMap<Integer, ArrayList<CustomInventory>> inventories = new HashMap<>();

	public FlexibleSmartInventory(String mainName){
	this.mainName = mainName;
	}

	public FlexibleSmartInventory clone(){
		FlexibleSmartInventory clone = new FlexibleSmartInventory(mainName);
		clone.inventories = (HashMap<Integer, ArrayList<CustomInventory>>) inventories.clone();
		return clone;
	}

	public int addInventory(String name, int neededSpace, HashMap<Integer, ItemStack> specialItems){
		InventorySize selectedSize = InventorySize.SMALL;
		if(selectedSize.size < neededSpace) selectedSize = InventorySize.MEDIUM;
		if(selectedSize.size < neededSpace) selectedSize = InventorySize.LARGE;
		if(selectedSize.size < neededSpace) selectedSize = InventorySize.MAX;
		return addInventory(name, selectedSize, specialItems);
	}

	public int addInventory(String name, InventorySize size, HashMap<Integer, ItemStack> specialItems){
	int id = inventories.size();

	CustomInventory customInventory = new CustomInventory(mainName + (name.isEmpty() ? "" : ": " + name), size, specialItems, id, 0);
	ArrayList<CustomInventory> list = new ArrayList<>();
	list.add(customInventory);

	if(id > 0){
		customInventory.addNavigationButtons(false, true);
		getLastInventory(id - 1).addNavigationButtons(true, false);
	}

	inventories.put(id, list);
	return id;
	}

	private CustomInventory getLastInventory(int id){
		ArrayList<CustomInventory> list = inventories.get(id);
		return list.get(list.size() - 1);
	}

	public ArrayList<PositionData> prepareForItems(int id, int requiredAmount){
		ArrayList<PositionData> list = new ArrayList<>();
		ArrayList<CustomInventory> cilist = inventories.get(id);

		int slotsNeeded = requiredAmount;
		HashMap<Integer, ArrayList<Integer>> emptySlots = new HashMap<>();

		for(CustomInventory ci: cilist){ //Loop through current inventories and expand as needed.
			ArrayList<Integer> eslots = ci.getEmptySlots();
			while(slotsNeeded > eslots.size() && !ci.size.equals(InventorySize.MAX)){
				ci.expand();
				eslots = ci.getEmptySlots();
			}
			emptySlots.put(ci.subId, eslots);
			slotsNeeded -= eslots.size();

			if(slotsNeeded < 1) break;
		}

		if(slotsNeeded > 0){ //We still need space, add new inventory
			getLastInventory(id).addInventory(InventorySize.SMALL, inventories);
			return prepareForItems(id, requiredAmount);
		}

		for(int subId: emptySlots.keySet()){
			for(int slot: emptySlots.get(subId)) {
				list.add(new PositionData(id, subId, slot));
			}
		}

		return list;
	}

	public void removeItem(int id, String cleanName){
		for(CustomInventory ci: inventories.get(id)){
			for(ItemStack item: ci.getContents()){
				if(ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(cleanName)){
					ci.removeItem(item);
					break;
				}
			}
		}
	}

	public void removeItem(PositionData pd){
		removeItem(pd.id, pd.subId, pd.slot);
	}

	public void removeItem(int id, int subId, int slot){
		inventories.get(id).get(subId).removeItem(slot);
	}

//	public void clear(int id){
//		for(CustomInventory ci: inventories.get(id)){
//			ci.clear();
//		}
//	}

	public List<ItemStack> getContents(){
		ArrayList<ItemStack> items = new ArrayList<>();
		for(int i = 0; i < inventories.size(); i++){
			for(CustomInventory ci: inventories.get(i)){
				items.addAll(ci.getContents());
			}
		}
		return items;
	}

	public List<ItemStack> getContents(int id){
		ArrayList<ItemStack> items = new ArrayList<>();
			for(CustomInventory ci: inventories.get(id)){
				items.addAll(ci.getContents());
			}
		return items;
	}

	public void setItem(int id, int subId, int slot, ItemStack item){
		inventories.get(id).get(subId).setItem(slot, item);
	}

	public PositionData addItem(ItemStack item, int id){
		return getLastInventory(id).addItem(item, id, inventories.get(id).size() - 1, inventories);
	}

	public ItemStack getItem(int id, int subId, int slot){
		return inventories.get(id).get(subId).inventory.getItem(slot);
	}

	public void open(Player p){
		open(p, 0, 0);
	}

	public void open(Player p, int id, int subId){
		inventories.get(id).get(subId).open(p);
	}

	public boolean handleClick(Player p, ItemStack clicked, Inventory inv, int slot){
		if(slot < 0 || slot >= inv.getSize()) return true; //Out of bonds. Say that we handled it and do nothing.
		if(!Utils.compareItem(clicked, Customization.getInstance().items.get("Next")) && !Utils.compareItem(clicked, Customization.getInstance().items.get("Previous"))) return false;

		for(int i = 0; i < inventories.size(); i++){
			List<CustomInventory> list = inventories.get(i);
			for(CustomInventory ci: list){
				if(ci.inventory.equals(inv)){

					int targetId, targetSubId;
					if(Utils.compareItem(clicked, Customization.getInstance().items.get("Next"))){

						if(ci.subId + 1 < list.size()){
							targetId = ci.id;
							targetSubId = ci.subId + 1;
						} else {
							targetId = ci.id + 1;
							targetSubId = 0;
						}

					} else {

						if(ci.subId == 0){
							targetId = ci.id - 1;
							targetSubId = inventories.get(i - 1).size() - 1;
						} else {
							targetId = ci.id;
							targetSubId = ci.subId - 1;
						}

					}

					open(p, targetId, targetSubId);
					break;
				}
			}
		}
		return true;
	}
	
	public String getName(){
	return mainName;
	}

	/*



	Main class ends



	 */

	public class CustomInventory {

		int id, subId;
		boolean hasNextButton, hasBackButton;

		String title;

		Inventory inventory;
		ArrayList<Integer> slots;
		InventorySize size;
		HashMap<Integer, ItemStack> specialItems;

		public CustomInventory(String title, InventorySize size, HashMap<Integer, ItemStack> specialItems, int id, int subId){
			setupCustomInventory(title, size, specialItems, id, subId);
		}

		public void setupCustomInventory(String title, InventorySize size, HashMap<Integer, ItemStack> specialItems, int id, int subId){
			this.title = title;
			this.size = size;
			this.specialItems = specialItems;
			this.id = id;
			this.subId = subId;

			inventory = Bukkit.createInventory(null, size.size, title);
			Utils.cageInventory(inventory, false);

			if(specialItems != null) for(int slot: specialItems.keySet()){
				inventory.setItem(slot + inventory.getSize() - 9, specialItems.get(slot));
			}

			slots = new ArrayList<>();
			for(int i = 0; i < inventory.getSize(); i++){
				if(inventory.getItem(i) == null) slots.add(i);
			}
		}

		public void open(Player p){
			p.openInventory(inventory);
		}

		public void addNavigationButtons(boolean next, boolean back){

			hasNextButton = next;
			hasBackButton = back;

			int buttonsCount = 0;
			int firstSlot = 0;

			if(size.equals(InventorySize.SMALL)){
				buttonsCount = 1;
				firstSlot = 9;
			} else if(size.equals(InventorySize.MEDIUM)){
				buttonsCount = 2;
				firstSlot = 9;
			} else if(size.equals(InventorySize.LARGE)){
				buttonsCount = 3;
				firstSlot = 9;
			} else if(size.equals(InventorySize.MAX)){
				buttonsCount = 2;
				firstSlot = 18;
			}

			for(int i = 0; i < buttonsCount; i++){
				if(back) inventory.setItem(firstSlot + i * 9, Customization.getInstance().items.get("Previous"));
				if(next) inventory.setItem(firstSlot + i * 9 + 8, Customization.getInstance().items.get("Next"));
			}
		}

		public boolean expand(){
			if(size.equals(InventorySize.MAX)) return false;

			InventorySize newSize;
			if(size.equals(InventorySize.SMALL)) newSize = InventorySize.MEDIUM;
			else if(size.equals(InventorySize.MEDIUM)) newSize = InventorySize.LARGE;
			else newSize = InventorySize.MAX;

			List<ItemStack> items = getContents();

			setupCustomInventory(title, newSize, specialItems, id, subId);
			addNavigationButtons(hasNextButton, hasBackButton);

			int i = 0;
			for(ItemStack item: items){
				inventory.setItem(slots.get(i), item);
				i++;
			}

			return true;
		}

//		public void maximize(){
//			List<ItemStack> items = getContents();
//
//			setupCustomInventory(title, InventorySize.MAX, specialItems, id, subId);
//
//			int i = 0;
//			for(ItemStack item: items){
//				inventory.setItem(slots.get(i), item);
//				i++;
//			}
//		}

		public void setItem(int slot, ItemStack item){
			inventory.setItem(slot, item);
		}

		//Maybe you can access inventories without even passing it here since this class is a child
		public PositionData addItem(ItemStack item, int id, int subId, HashMap<Integer, ArrayList<CustomInventory>> masterList){ //Id and master list only needed for creating new inventory
			int emptySlot = getEmptySlot();
			if(emptySlot != -1){
				inventory.setItem(emptySlot, item);
				return new PositionData(id, subId, emptySlot);
			} else {
					boolean expanded = expand();
					if(expanded) {
						emptySlot = getEmptySlot();
						inventory.setItem(emptySlot, item);
						return new PositionData(id, subId, emptySlot);
					} else {
						//Unable to expand, create new inventory in the master list
						CustomInventory newInventory = addInventory(InventorySize.SMALL, masterList);

						return newInventory.addItem(item, id, subId + 1, masterList);
					}
			}
		}

		public CustomInventory addInventory(InventorySize size, HashMap<Integer, ArrayList<CustomInventory>> masterList){
			CustomInventory newInventory = new CustomInventory(title, size, specialItems, id, subId + 1);
			newInventory.addNavigationButtons(false, true);

			ArrayList<CustomInventory> list =  masterList.get(id);
			list.add(newInventory);
			masterList.put(id, list);

			addNavigationButtons(true, false);
			return newInventory;
		}

		public void clear(){
			for(int slot: slots) inventory.setItem(slot, new ItemStack(Material.AIR));
		}

		public ArrayList<Integer> getEmptySlots(){
			ArrayList<Integer> list = new ArrayList<>();
			for(int i: slots) if(inventory.getItem(i) == null) list.add(i);
			return list;
		}

		public int getEmptySlot(){
			for(int i: slots) if(inventory.getItem(i) == null) return i;
			return -1;
		}

		public List<ItemStack> getContents(){
			List<ItemStack> items = new ArrayList<>();
			for(int slot: slots){
				ItemStack item = inventory.getItem(slot);
				if(item != null) items.add(item);
			}
			return items;
		}

		public void organize(){
			ArrayList<ItemStack> items = new ArrayList<>();
			for(int i: slots){
				ItemStack item = inventory.getItem(i);
				if(item != null){
					items.add(item);
					inventory.setItem(i, new ItemStack(Material.AIR));
				}
			}
			int i = 0;
			for(ItemStack item: items){
				inventory.setItem(slots.get(i), item);
				i++;
			}
		}

		public void removeItem(ItemStack target){
			for(int slot: slots){
				ItemStack item = inventory.getItem(slot);
				if(item != null && item.equals(target)){
					removeItem(slot);
					break;
				}
			}
		}

		public void removeItem(int slot){
			inventory.setItem(slot, new ItemStack(Material.AIR));
			organize();
		}

	}

	public class PositionData {
		public int id, subId, slot;

		public PositionData(int id, int subId, int slot){
			this.id = id;
			this.subId = subId;
			this.slot = slot;
		}
	}

	public enum InventorySize {

		SMALL(27), MEDIUM(36), LARGE(45), MAX(54);

		int size;
		InventorySize(int size){
			this.size = size;
		}

	}

}
