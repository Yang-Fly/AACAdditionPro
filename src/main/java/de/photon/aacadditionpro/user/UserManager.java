package de.photon.aacadditionpro.user;

import com.comphenix.protocol.events.PacketEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserManager implements Listener
{
    // Concurrency to tackle some ConcurrentModificationExceptions
    private static final ConcurrentMap<UUID, User> users = new ConcurrentHashMap<>(1024);
    @Getter
    private static final Set<User> verboseUsers = ConcurrentHashMap.newKeySet(64);

    public static User getUser(final UUID uuid)
    {
        return users.get(uuid);
    }

    public static User safeGetUserFromPacketEvent(PacketEvent event)
    {
        // Special handling here as a player could potentially log out after this and therefore cause a NPE.
        final Player player = event.getPlayer();
        if (event.isPlayerTemporary() || player == null) {
            return null;
        }

        return UserManager.getUser(player.getUniqueId());
    }

    /**
     * Gets all {@link User}s wrapped in a {@link HashSet}. <br>
     * Safe to modify.
     */
    public static Collection<User> getUsers()
    {
        return new HashSet<>(getUsersUnwrapped());
    }

    /**
     * Gets all {@link User}s without wrapping. <br>
     * DO NOT MODIFY THIS COLLECTION; IT WILL MESS UP THE USER MANAGEMENT.
     * <p>
     * Use this solely for performance purposes e.g. in iterations or as a source {@link Collection} for wrapping.
     */
    public static Collection<User> getUsersUnwrapped()
    {
        return users.values();
    }

    public static boolean isVerbose(final User user)
    {
        return verboseUsers.contains(user);
    }

    /**
     * Sets the verbose state of an {@link User}.
     */
    public static void setVerbose(final User user, final boolean verbose)
    {
        if (verbose) {
            verboseUsers.add(user);
        } else {
            verboseUsers.remove(user);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event)
    {
        users.put(event.getPlayer().getUniqueId(), new User(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event)
    {
        final User removedUser = users.remove(event.getPlayer().getUniqueId());
        verboseUsers.remove(removedUser);
        removedUser.unregister();
    }
}
