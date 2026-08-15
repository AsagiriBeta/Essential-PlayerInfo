package com.jackdaw.essentialinfo.module.message;

import com.google.inject.Inject;
import com.jackdaw.essentialinfo.auxiliary.configuration.SettingManager;
import com.jackdaw.essentialinfo.auxiliary.serializer.Deserializer;
import com.jackdaw.essentialinfo.module.AbstractComponent;
import com.jackdaw.essentialinfo.module.VelocityDataDir;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Message extends AbstractComponent {
    // class for Server
    private final Parser parser = MessageParser.getParser();
    private final boolean isCommandToBroadcast;
    private final boolean isCustomTextEnabled;
    private final String chatText;

    @Inject
    public Message(ProxyServer proxyServer, Logger logger, @VelocityDataDir Path velocityDataDir, SettingManager setting) {
        super(proxyServer, logger, velocityDataDir, setting);
        this.isCommandToBroadcast = setting.isCommandToBroadcastEnabled();
        this.isCustomTextEnabled = setting.isCustomTextEnabled();
        this.chatText = setting.getChatText();
    }

    // listener of player chat
    // Do not deny/modify PlayerChatEvent result: on 1.19.1+ that can kick players (signed chat).
    @Subscribe(priority = 100)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (this.isCommandToBroadcast) {
            Map<String, Object> parsedMessage = parser.parse(message);
            Object broadcastTag = parsedMessage.get("broadcastTag");
            if (Boolean.TRUE.equals(broadcastTag)) {
                Object content = parsedMessage.get("content");
                if (content != null) {
                    broadcast(player, content.toString());
                }
            }
        } else {
            broadcast(player, message);
        }

    }

    // broadcast the message
    private void broadcast(Player player, String message) {
        String playerName = player.getUsername();
        String sendMessage;
        Optional<ServerConnection> serverConnection = player.getCurrentServer();
        Optional<String> currentServerName = serverConnection
                .map(connection -> connection.getServerInfo().getName());
        Optional<RegisteredServer> currentServer = serverConnection
                .map(ServerConnection::getServer);
        // Audience message
        if (currentServerName.isPresent()) {
            String server = currentServerName.get();
            if (this.isCustomTextEnabled) {
                if (this.chatText.isEmpty()) return;
                sendMessage = this.chatText.replace("%player%", playerName).replace("%server%", server) + message;
            } else {
                sendMessage = String.join(""
                        , "<gray><u><click:run_command:'/server "
                        , server
                        , "'><hover:show_text:'点击切换服务器。'>["
                        , server
                        , "]</hover></click></u> <"
                        , playerName
                        , "> "
                        , message
                );
            }
        } else {
            sendMessage = "<" + player.getUsername() + "> " + message;
        }
        // send message to other server
        for (RegisteredServer s : this.proxyServer.getAllServers()) {
            if (currentServer.isEmpty() || !Objects.equals(s, currentServer.get())) {
                s.sendMessage(Deserializer.miniMessage(sendMessage));
            }
        }
    }
}
