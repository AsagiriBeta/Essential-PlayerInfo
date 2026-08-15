package com.jackdaw.essentialinfo;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jackdaw.essentialinfo.auxiliary.configuration.SettingManager;
import com.jackdaw.essentialinfo.module.connectMessage.ConnectionMessage;
import com.jackdaw.essentialinfo.module.AbstractComponent;
import com.jackdaw.essentialinfo.module.JackdawModule;
import com.jackdaw.essentialinfo.module.connectionTips.ConnectionTips;
import com.jackdaw.essentialinfo.module.message.Message;
import com.jackdaw.essentialinfo.module.pinglist.PingList;
import com.jackdaw.essentialinfo.module.rememberMe.RememberMe;
import com.jackdaw.essentialinfo.module.tablist.TabList;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

// register the plugin
@Plugin(
        id = "essential-info",
        name = "Essential Info",
        version = BuildConstants.VERSION,
        authors = {"Team-Jackdaw"},
        description = "Lightweight global tablist, chat and connection tips for Velocity 4."
)
public class EssentialInfo {

    // Injection done by Velocity
    private final ProxyServer proxyServer;
    private final Logger logger;                // slf4j Logger
    private final Path dataDirectory;           // Path of the plugin

    private final SettingManager setting;
    private final Injector injector;

    // connect to the server and logger
    @Inject
    public EssentialInfo(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.setting = getSettingManager();
        this.injector = Guice.createInjector(new JackdawModule(this, proxyServer, logger, dataDirectory, this.setting));
    }

    // register the listeners
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (setting == null) {
            logger.error("主程序：无法加载配置文件。");
            return;
        }

        if (setting.isTabListEnabled()) {
            TabList tabList = injector.getInstance(TabList.class);
            this.moduleOn(tabList, "主程序：已加载 TabList。");
            this.proxyServer.getScheduler().buildTask(this, tabList::pingUpdate)
                .repeat(50L, java.util.concurrent.TimeUnit.MILLISECONDS).schedule();
        }

        if (setting.isMessageEnabled()) {
            this.moduleOn(injector.getInstance(Message.class), "主程序：已加载 Message。");
        }

        if (setting.isPingListEnabled()) {
            this.moduleOn(injector.getInstance(PingList.class), "主程序：已加载 PingList。");
        }

        if (setting.isConnectionTipsEnabled()) {
            this.moduleOn(injector.getInstance(ConnectionTips.class), "主程序：已加载 ConnectionTips。");
        }

        if (setting.isRememberMeEnabled()) {
            this.moduleOn(injector.getInstance(RememberMe.class), "主程序：已加载 RememberMe。");
        }

        if (setting.isConnectionMessageEnabled()) {
            this.moduleOn(injector.getInstance(ConnectionMessage.class), "主程序：已加载 ConnectionMessage。");
        }
    }

    @Nullable
    private SettingManager getSettingManager() {
        SettingManager setting;
        try {
            setting = new SettingManager(dataDirectory.toFile(), logger);
        } catch (IOException ioException) {
            logger.error("主程序：读取配置失败。", ioException);
            return null;
        }
        return setting;
    }

    private void moduleOn(AbstractComponent module, String log){
        this.proxyServer.getEventManager().register(this, module);
        this.logger.info(log);
    }

}
