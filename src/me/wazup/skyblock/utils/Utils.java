package me.wazup.skyblock.utils;

import com.google.common.collect.Lists;
import me.wazup.skyblock.managers.Customization;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Utils {

    public static final Random random = new Random();

    private static final ChatColor[] goodColors = {ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW};
    public static ChatColor getRandomColor() {
        return goodColors[random.nextInt(goodColors.length)];
    }

    public static List<Player> getOnlinePlayers(){
        List<Player> list = Lists.newArrayList();
        for (World world : Bukkit.getWorlds()) list.addAll(world.getPlayers());
        return Collections.unmodifiableList(list);
    }

    public static void createPlayerBorder(Player p, Location borderCenter, int borderSize) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        //Kept for future reference
        //        WorldBorder wb = new WorldBorder();
        //        wb.setCenter(borderCenter.getBlockX(), borderCenter.getBlockZ());
        //        wb.setSize(borderSize);
        //        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);

        Class<?> worldBorderClass = ReflectionUtils.getNMSClass("WorldBorder");
        Object worldBorderObject = worldBorderClass.getConstructor().newInstance();
        ReflectionUtils.getMethod(worldBorderClass, "setCenter").invoke(worldBorderObject, borderCenter.getBlockX(), borderCenter.getBlockZ());
        ReflectionUtils.getMethod(worldBorderClass, "setSize").invoke(worldBorderObject, borderSize);

        Class<?> packetClass = ReflectionUtils.getNMSClass("PacketPlayOutWorldBorder");
        Class enumWorldBorderAction = ReflectionUtils.getNMSClass("PacketPlayOutWorldBorder$EnumWorldBorderAction");
        Constructor<?> packetConstructor = packetClass.getConstructor(worldBorderClass, enumWorldBorderAction);
        Enum<?> e = Enum.valueOf(enumWorldBorderAction, "INITIALIZE");
        Object packetObject = packetConstructor.newInstance(worldBorderObject, e);

        ReflectionUtils.sendPacket(p, packetObject);
    }

    public static ItemStack getItemStack(String item, boolean amount, boolean extra){
        String[] split = item.split(" : ");
        String firstSplitUpperCase = split[0].toUpperCase();

        ItemStackBuilder builder;

        Optional<XMaterial> xmaterial = XMaterial.matchXMaterial(firstSplitUpperCase);
        if (!xmaterial.isPresent() && firstSplitUpperCase.contains(":")) xmaterial = XMaterial.matchXMaterial(firstSplitUpperCase.split(":")[0]);
        builder = new ItemStackBuilder(xmaterial.get().parseItem());
        if (split[0].contains(":")) {
            String materialType = firstSplitUpperCase.split(":")[0];
            if ((materialType.contains("POTION")) && firstSplitUpperCase.split(":").length == 4) {
                builder.setPotionEffect(PotionType.valueOf(firstSplitUpperCase.split(":")[1]), Boolean.parseBoolean(split[0].split(":")[2]), Boolean.parseBoolean(split[0].split(":")[3]));
            } else builder.setDurability(Integer.parseInt(firstSplitUpperCase.split(":")[1]));
        }

        if(amount) builder.setAmount(Integer.parseInt(split[1]));
        if(extra){
            for(int i = amount ? 2 : 1; i < split.length; i++){
                String type = split[i].split(":")[0].toLowerCase();
                if(type.equals("name")) builder.setName(ChatColor.translateAlternateColorCodes('&', split[i].split(":")[1])); else
                if(type.equals("lore")) builder.addLore(ChatColor.translateAlternateColorCodes('&', split[i].split(":")[1])); else
                if(type.equals("enchant")) builder.addEnchantment(Enchantment.getByName(split[i].split(":")[1].toUpperCase()), Integer.parseInt(split[i].split(":")[2]));
            }
        }
        return builder.build();
    }

    public static List<Player> getPlayers(Collection<UUID> list){
        List<Player> players = new ArrayList<>();
        for(UUID player: list) if(Bukkit.getPlayer(player) != null) players.add(Bukkit.getPlayer(player));
        return players;
    }

    public static void clearPlayer(Player p){
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setMaxHealth(20);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setFireTicks(0);
        p.setLevel(0);
        p.setExp(0);
        p.setGameMode(GameMode.SURVIVAL);
        p.setAllowFlight(false);
        p.setFlying(false);
        for(PotionEffect effect : p.getActivePotionEffects()){
            p.removePotionEffect(effect.getType());
        }
    }

    public static String getStringFromExactLocation(Location l){
        return l.getWorld().getName() + ", " + l.getX() + ", " + l.getY() + ", " + l.getZ() + ", " + l.getYaw() + ", " + l.getPitch();
    }

    public static String getStringFromLocation(Location l, boolean center){
        return l.getWorld().getName() + ", " + (l.getBlockX() + (center ? 0.5 : 0)) + ", " + (l.getBlockY() + (center ? 1 : 0)) + ", " + (l.getBlockZ() + (center ? 0.5 : 0)) + ", " + l.getYaw() + ", " + l.getPitch();
    }

    public static Location getLocationFromString(String l){
        String[] split = l.split(", ");
        World w = Bukkit.getWorld(split[0]);
        double x = Double.parseDouble(split[1]), y = Double.parseDouble(split[2]), z = Double.parseDouble(split[3]);
        float yaw = Float.parseFloat(split[4]), pitch = Float.parseFloat(split[5]);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public static String getReadableLocationString(Location l, boolean center){
        return "" + ChatColor.GREEN + (l.getBlockX() + (center ? 0.5 : 0)) + ChatColor.GRAY + ", " + ChatColor.GREEN + (l.getBlockY() + (center ? 1 : 0)) + ChatColor.GRAY + ", " + ChatColor.GREEN + (l.getBlockZ() + (center ? 0.5 : 0));
    }

    public static boolean checkNumbers(String... x){
        try {
            for(String o: x) Integer.parseInt(o);
        } catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    public static boolean checkDoubles(String... x){
        try {
            for(String o: x) Double.parseDouble(o);
        } catch (NumberFormatException e){
            return false;
        }
        return true;
    }

    public static void cageInventory(Inventory inv, boolean full){
        ItemStack pane_itemstack = Customization.getInstance().items.get("Pane");

        if(full){
            for(int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane_itemstack);
            return;
        }

        for(int i = 0; i < 9; i++) inv.setItem(i, pane_itemstack);
        for(int i = inv.getSize() - 9; i < inv.getSize(); i++) inv.setItem(i, pane_itemstack);
        int rows = (inv.getSize() / 9) - 2;
        if(rows < 1) return;
        for(int i = 9; i < ((9 * rows) + 1); i += 9) inv.setItem(i, pane_itemstack);
        for(int i = 17; i < (9 * (rows + 1)); i += 9) inv.setItem(i, pane_itemstack);
    }

    public static boolean compareItem(ItemStack item1, ItemStack item2){
        return item1 != null && item2 != null && item1.getType().equals(item2.getType()) && item1.getItemMeta().equals(item2.getItemMeta());
    }

    public static void sendCommandUsage(CommandSender sender, String commandName, String arguments){
        sender.sendMessage(Customization.getInstance().prefix + "Usage: /Skyblock " + ChatColor.GREEN + commandName + ChatColor.YELLOW + " " + arguments);
    }

    public static void error(String message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Skyblock X] " + message);
    }

    public static Inventory getBlockInventory(Block b){
        if(XMaterial.isNewVersion()){
            if(b.getState() instanceof Container){
                return ((Container) b.getState()).getInventory();
            }
        } else {
            if(b.getType().equals(Material.CHEST)){
                return ((Chest) b.getState()).getInventory();
            }
        }
        return null;
    }

    public static byte getByteFromFace(BlockFace face) {
        switch (face) {
            case EAST:
                return (byte) 2;
            case SOUTH:
                return (byte) 3;
            case WEST:
                return (byte) 4;
            default: //North
                return (byte) 0;
        }

    }

    public static BlockFace getFaceFromByte(byte id) {
        switch (id) {
            case 2:
                return BlockFace.EAST;
            case 3:
                return BlockFace.SOUTH;
            case 4:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }

    }

}
