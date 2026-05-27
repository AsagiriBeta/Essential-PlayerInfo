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
import java.util.HashMap;
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
    @Subscribe(priority = 100)
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (this.isCommandToBroadcast) {
            HashMap parsedMessage = parser.parse(message);
            if (parsedMessage.get("broadcastTag").equals(true)) {
                broadcast(player, parsedMessage.get("content").toString());
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
                .map(serverConnection -> serverConnection.getServerInfo().getName());
        Optional<RegisteredServer> currentServer = serverConnection
                .map(serverConnection -> serverConnection.getServer());
        // Audience message
        if (currentServerName.isPresent()) {
            String server = currentServerName.get();
            if (this.isCustomTextEnabled) {
                if (this.chatText.isEmpty()) return;
                sendMessage = this.chatText.replace("%player%", playerName).replace("%server%", server) + message;
            } else {
                // "<gray><u><click:run_command:'/server %server%'><hover:show_text:'Click to switch.'>[%server%]</hover></click></u> <%player%> "
                sendMessage = String.join(""
                        , "<gray><u><click:run_command:'/server "
                        , server
                        , "'><hover:show_text:'Click to switch.'>["
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
