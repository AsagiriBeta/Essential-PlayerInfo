package com.jackdaw.essentialinfo.module.connectMessage;

import com.google.inject.Inject;
import com.jackdaw.essentialinfo.auxiliary.configuration.SettingManager;
import com.jackdaw.essentialinfo.auxiliary.serializer.Deserializer;
import com.jackdaw.essentialinfo.module.AbstractComponent;
import com.jackdaw.essentialinfo.module.VelocityDataDir;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This module will use miniMessage module to deserializer the connection message.
 */
public class ConnectionMessage extends AbstractComponent {
    private final boolean isCustomTextEnabled;
    private final String connectionMessageText;
    private final String serverName;

    @Inject
    public ConnectionMessage(ProxyServer proxyServer, Logger logger, @VelocityDataDir Path velocityDataDir, SettingManager setting) {
        super(proxyServer, logger, velocityDataDir, setting);
        this.isCustomTextEnabled = setting.isCustomTextEnabled();
        this.connectionMessageText = setting.getConnectionMessageText();
        this.serverName = setting.getServerName();
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent event) {
        // Only greet on the first backend connection, not every server switch.
        if (event.getPreviousServer().isPresent()) {
            return;
        }
        connectMessage(event);
    }

    private void connectMessage(@NotNull ServerConnectedEvent event) {
        // send the connection message to the player who just connects to the register server.
        Player player = event.getPlayer();
        RegisteredServer currentServer = event.getServer();
        String sendMessage;
        List<String> serverList = proxyServer
                .getAllServers()
                .stream()
                .map(s -> s.getServerInfo().getName())
                .collect(Collectors.toList());
        if (isCustomTextEnabled) {
            if (connectionMessageText.isEmpty()) return;
            sendMessage = connectionMessageText
                    .replace("%serverName%", serverName)
                    .replace("%player%", player.getUsername())
                    .replace("%server%", currentServer.getServerInfo().getName())
                    .replace("%serverList%", serverListMap(serverList, currentServer.getServerInfo().getName()));
        } else {
            // default connection message
            sendMessage = String.join(""
                    , "<yellow>-------------------------------\n欢迎来到 "
                    , serverName
                    , "\n-------------------------------\n你可以点击下方服务器名称切换连接的服务器。\n"
                    , serverListMap(serverList, currentServer.getServerInfo().getName())
                    , "\n-------------------------------\n你可以使用 <u><light_purple><click:run_command:'/remember'><hover:show_text:'点击执行命令'>/remember</hover></click></light_purple></u> 设置默认连接服务器。\n-------------------------------</yellow>"
            );
        }
        player.sendMessage(Deserializer.miniMessage(sendMessage));
    }

    private @NotNull String serverListMap(@NotNull List<String> serverList, String currentServer) {
        // mapping the serverList to a single String and high line the currentServer.
        StringBuilder str = new StringBuilder();
        for (String serverName : serverList) {
            if (serverName.equals(currentServer)) {
                str.append("<red><u>[").append(serverName).append("]</u></red>");
            } else {
                str.append("<green><hover:show_text:'点击连接到 [")
                        .append(serverName)
                        .append("]'><click:run_command:'/server ")
                        .append(serverName)
                        .append("'>[")
                        .append(serverName)
                        .append("]</click></hover></green>");
            }
        }
        return str.toString();
    }
}
