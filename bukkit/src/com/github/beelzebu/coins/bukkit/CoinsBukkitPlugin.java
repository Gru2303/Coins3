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
package com.github.beelzebu.coins.bukkit;

import com.github.beelzebu.coins.api.config.CoinsConfig;
import com.github.beelzebu.coins.bukkit.listener.CommandListener;
import com.github.beelzebu.coins.bukkit.listener.GUIListener;
import com.github.beelzebu.coins.bukkit.listener.LoginListener;
import com.github.beelzebu.coins.bukkit.listener.SignListener;
import com.github.beelzebu.coins.bukkit.menus.CoinsMenu;
import com.github.beelzebu.coins.bukkit.utils.CoinsEconomy;
import com.github.beelzebu.coins.bukkit.utils.CompatUtils;
import com.github.beelzebu.coins.bukkit.utils.leaderheads.LeaderHeadsHook;
import com.github.beelzebu.coins.bukkit.utils.placeholders.CoinsPlaceholders;
import com.github.beelzebu.coins.bukkit.utils.placeholders.MultipliersPlaceholders;
import com.github.beelzebu.coins.common.plugin.CommonCoinsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class CoinsBukkitPlugin extends CommonCoinsPlugin<CoinsBukkitMain> {

    private boolean vault = false, placeholderapi = false, leaderheads = false;
    private CoinsEconomy coinsEconomy;

    CoinsBukkitPlugin(@NotNull CoinsBukkitMain bootstrap, CoinsConfig coinsConfig) {
        super(bootstrap, coinsConfig);
    }

    @Override
    public void load() {
        super.load();
        hookOptionalDependencies();
    }

    @Override
    public void enable() {
        super.enable();
        if (getConfig() == null) {
            return;
        }
        CompatUtils.setup();
        hookOptionalDependencies();
        // Create the command
        getBootstrap().getCommandManager().registerCommand();
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), getBootstrap());
        Bukkit.getPluginManager().registerEvents(new GUIListener(), getBootstrap());
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), getBootstrap());
        Bukkit.getPluginManager().registerEvents(new SignListener(this), getBootstrap());
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginEnable(@NotNull PluginEnableEvent e) { // keep trying to hook with optional dependencies
                if (getBootstrap().getDescription().getSoftDepend().stream().anyMatch(hook -> e.getPlugin().getName().equalsIgnoreCase(hook))) {
                    hookOptionalDependencies();
                }
            }
        }, getBootstrap());
    }

    @Override
    public void disable() {
        super.disable();
        if (getConfig() == null) {
            return;
        }
        if (coinsEconomy != null) {
            coinsEconomy.shutdown();
        }
        CoinsMenu.getInventoriesByUUID().values().forEach(CoinsMenu::delete);
        getBootstrap().getCommandManager().unregisterCommand();
        Bukkit.getScheduler().cancelTasks(getBootstrap());
    }

    public CoinsEconomy getCoinsEconomy() {
        return coinsEconomy;
    }

    public void setCoinsEconomy(CoinsEconomy coinsEconomy) {
        this.coinsEconomy = coinsEconomy;
    }

    private void hookOptionalDependencies() {
        // Hook vault
        if (getConfig().getBoolean("Vault.Use", false)) {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                if (!vault) {
                    log("Vault found, hooking into it.");
                    coinsEconomy = new CoinsEconomy(getBootstrap());
                    coinsEconomy.setup();
                    vault = true;
                }
            } else {
                log("You enabled Vault in the config, but the plugin Vault can't be found.");
            }
        }
        // Hook placeholders
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && !placeholderapi) {
            getBootstrap().log("PlaceholderAPI found, hooking into it.");
            new CoinsPlaceholders().register();
            new MultipliersPlaceholders(this).register();
            placeholderapi = true;
        }
        // Hook with LeaderHeads
        if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads") && !leaderheads) {
            getBootstrap().log("LeaderHeads found, hooking into it.");
            new LeaderHeadsHook();
            leaderheads = true;
        }
    }
}
