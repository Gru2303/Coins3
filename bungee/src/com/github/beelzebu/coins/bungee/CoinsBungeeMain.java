/*
 * This file is part of coins3
 *
 * Copyright © 2020 Beelzebu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.beelzebu.coins.bungee;

import com.github.beelzebu.coins.api.Multiplier;
import com.github.beelzebu.coins.api.config.AbstractConfigFile;
import com.github.beelzebu.coins.api.messaging.ProxyMessaging;
import com.github.beelzebu.coins.api.plugin.CoinsBootstrap;
import com.github.beelzebu.coins.api.utils.StringUtils;
import com.github.beelzebu.coins.bungee.config.BungeeCoinsConfig;
import com.github.beelzebu.coins.bungee.config.BungeeConfigFile;
import com.github.beelzebu.coins.bungee.messaging.BungeeMessaging;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class CoinsBungeeMain extends Plugin implements CoinsBootstrap {

    private CoinsBungeePlugin plugin;
    private BungeeMessaging messaging;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        plugin = new CoinsBungeePlugin(this, new BungeeCoinsConfig(new File(getDataFolder(), "config.yml")));
        plugin.load();
        plugin.enable();
    }

    @Override
    public void onDisable() {
        plugin.disable();
    }

    @Override
    public CoinsBungeePlugin getPlugin() {
        return plugin;
    }

    @NotNull
    @Override
    public AbstractConfigFile getFileAsConfig(File file) {
        return new BungeeConfigFile(file);
    }

    @Override
    public void runAsync(Runnable rn) {
        ProxyServer.getInstance().getScheduler().runAsync(this, rn);
    }

    @Override
    public void runSync(@NotNull Runnable rn) {
        rn.run();
    }

    @Override
    public void schedule(Runnable rn, long interval) {
        ProxyServer.getInstance().getScheduler().schedule(this, rn, interval * 50 /* 1 tick = 50 ms*/, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleAsync(Runnable rn, long interval) {
        ProxyServer.getInstance().getScheduler().schedule(this, rn, interval * 50 /* 1 tick = 50 ms*/, TimeUnit.MILLISECONDS);
    }

    @Override
    public void executeCommand(@NotNull String cmd) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand((CommandSender) getConsole(), cmd);
    }

    @Override
    public void log(String msg) {
        ((CommandSender) getConsole()).sendMessage(TextComponent.fromLegacyText(StringUtils.rep("&8[&cCoins&8] &7" + msg)));
    }

    @Override
    public Object getConsole() {
        return ProxyServer.getInstance().getConsole();
    }

    @Override
    public void sendMessage(@NotNull Object commandSender, @NotNull String msg) {
        ((CommandSender) commandSender).sendMessage(TextComponent.fromLegacyText(msg));
    }

    @Override
    public InputStream getResource(String filename) {
        return getResourceAsStream(filename);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return ProxyServer.getInstance().getPlayer(uuid) != null;
    }

    @Override
    public boolean isOnline(String name) {
        return ProxyServer.getInstance().getPlayer(name) != null;
    }

    @Nullable
    @Override
    public UUID getUUID(String name) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(name);
        return proxiedPlayer != null ? proxiedPlayer.getUniqueId() : null;
    }

    @Nullable
    @Override
    public String getName(UUID uuid) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
        return proxiedPlayer != null ? proxiedPlayer.getName() : null;
    }

    @Override
    public void callCoinsChangeEvent(UUID uuid, double oldCoins, double newCoins) {
        // TODO: re add
        //ProxyServer.getInstance().getPluginManager().callEvent(new CoinsChangeEvent(uuid, oldCoins, newCoins));
    }

    @Override
    public void callMultiplierEnableEvent(Multiplier multiplier) {
        // TODO: re add
        //ProxyServer.getInstance().getPluginManager().callEvent(new MultiplierEnableEvent(multiplier));
    }

    @NotNull
    @Override
    public List<String> getPermissions(UUID uuid) {
        List<String> permissions = new ArrayList<>();
        if (isOnline(uuid)) {
            permissions.addAll(ProxyServer.getInstance().getPlayer(uuid).getPermissions());
        }
        return permissions;
    }

    @NotNull
    @Override
    public ProxyMessaging getProxyMessaging() {
        return messaging == null ? messaging = new BungeeMessaging(plugin) : messaging;
    }
}
