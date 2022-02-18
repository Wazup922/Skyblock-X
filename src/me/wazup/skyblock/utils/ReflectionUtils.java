package me.wazup.skyblock.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

    public static void sendPacket(Player p, Object packet) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Object playerHandle = p.getClass().getMethod("getHandle").invoke(p);
        Object connection = playerHandle.getClass().getField("playerConnection").get(playerHandle);
        Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet"));

        sendPacket.invoke(connection, packet);
    }

    public static Method getMethod(Class<?> targetClass, String methodName){
        for(Method method: targetClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getNMSVersion() + "." + name);
    }

    public static String getNMSVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

}
