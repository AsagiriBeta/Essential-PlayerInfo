package com.jackdaw.essentialinfo.module.rememberMe;

import com.jackdaw.essentialinfo.auxiliary.serializer.Deserializer;
import com.jackdaw.essentialinfo.auxiliary.userInfo.UserInfoManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class CommandSet implements SimpleCommand {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final File workingDirectory;

    public CommandSet(ProxyServer proxyServer, Logger logger, File workingDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public void execute(@NotNull Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof Player player)) {
            source.sendMessage(Deserializer.miniMessage("<red>该命令仅限玩家使用。</red>"));
            return;
        }

        String[] args = invocation.arguments();
        UserInfoManager userInfoManager = new UserInfoManager(workingDirectory, logger, player);
        Component help = helpMessage(userInfoManager);
        if (args.length < 2) {
            source.sendMessage(help);
            return;
        }
        String command = args[0];
        String parameter = args[1];
        if (command.equals("mode")) {
            if (parameter.equalsIgnoreCase("preset") || parameter.equalsIgnoreCase("last")) {
                setMode(parameter, player, userInfoManager);
                source.sendMessage(Deserializer.miniMessage("<green>你的默认模式已设置为 <yellow>" + parameter + "</yellow>。</green>"));
                return;
            }
        }
        if (command.equals("server")) {
            RegisteredServer initialServer = this.proxyServer.getServer(parameter).orElse(null);
            if (initialServer != null) {
                setServer(parameter, userInfoManager);
                source.sendMessage(Deserializer.miniMessage("<green>你的默认服务器已设置为 <yellow>" + parameter + "</yellow>。</green>"));
                return;
            }
        }
        source.sendMessage(help);
    }

    private static @NotNull Component helpMessage(@NotNull UserInfoManager userInfoManager) {
        String server = userInfoManager.getUserInfo().getServer();
        if (server == null || server.isBlank()) {
            server = "未设置";
        }
        return Deserializer.miniMessage(String.join("",
                "<dark_gray><yellow>-------------------------------\n",
                "这是 Essential-PlayerInfo 的 RememberMe 模块。\n",
                "设置模式：<light_purple><u><click:suggest_command:'/remember mode '>/remember mode [last|preset]</click></u></light_purple>\n",
                "设置服务器：<light_purple><u><click:suggest_command:'/remember server '>/remember server [服务器名]</click></u></light_purple>\n",
                "你当前的默认模式为 <u><red>",
                userInfoManager.getUserInfo().getDefaultMode(),
                "</red></u>。\n你当前的初始服务器为 <red><u>",
                server,
                "</u></red>。\n-------------------------------</yellow></dark_gray>"));
    }

    @Override
    public @NotNull CompletableFuture<List<String>> suggestAsync(@NotNull Invocation invocation) {
        String[] currentArgs = invocation.arguments();
        List<String> suggest;
        if (currentArgs.length == 0) {
            return CompletableFuture.completedFuture(List.of("server", "mode"));
        } else if (currentArgs.length == 1) {
            suggest = List.of("server", "mode");
            return CompletableFuture.completedFuture(suggest
                    .stream()
                    .filter(s -> s.startsWith(invocation.arguments()[0]))
                    .collect(Collectors.toList()));
        } else if(currentArgs.length == 2) {
            if (currentArgs[0].equals("server")) {
                suggest = proxyServer
                        .getAllServers()
                        .stream()
                        .map(registeredServer -> registeredServer
                                .getServerInfo()
                                .getName())
                        .collect(Collectors.toList());
            } else if (currentArgs[0].equals("mode")) {
                suggest = List.of("last", "preset");
            } else suggest = List.of();

            return CompletableFuture.completedFuture(suggest
                    .stream()
                    .filter(s -> s.startsWith(invocation.arguments()[1]))
                    .collect(Collectors.toList()));
        } else return CompletableFuture.completedFuture(List.of());
    }

    // set the default server by command
    public void setServer(String serverName, @NotNull UserInfoManager userInfoManager) {
        userInfoManager.setDefaultMode("preset");
        userInfoManager.setUserServer(serverName);
    }

    // set the default mode by command
    public void setMode(String mode, @NotNull Player player, @NotNull UserInfoManager userInfoManager) {
        userInfoManager.setDefaultMode(mode);
        if (player.getCurrentServer().isPresent()) {
            userInfoManager.setUserServer(player.getCurrentServer().get().getServerInfo().getName());
        } else userInfoManager.setUserServer(null);
    }
}
