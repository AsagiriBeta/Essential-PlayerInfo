# Essential-PlayerInfo

## Introduce

这是一个轻量级的 Velocity 代理端插件。

基于 **Velocity API 4.1.0-SNAPSHOT**（对应 Velocity 4.x）。官方稳定版为 **4.0.0**，开发文档当前推荐依赖为 `4.1.0-SNAPSHOT`。Velocity 4.x 需要 **Java 25+**。

## Core Features

### Global PingList

在 Minecraft 服务器列表中显示在线玩家 ID。

![PingList.png][1]

### Global TabList

在 Tab 列表中显示全服玩家。

- 可自定义显示模式与显示文本（见配置）。

![TabList2.png][2]

### Connection Message

玩家首次连接到后端服务器时发送欢迎消息，并可点击切换服务器。

- 可在配置文件中自定义消息。

![ConnectMessage.png][3]

### Global Chat & ConnectionTips

基础跨服聊天与进服/切换提示。

- 可点击 _[服务器名]_ 切换到目标服务器。

![customText2.png][4]

### Remember my initial connecting server

让玩家自行设置初始连接服务器。默认行为是“记住上次连接的服务器”。

玩家可用命令设置模式与服务器：

- 设置默认模式
  - 说明：设为 `preset` 时，每次进服都会连接到指定服务器；设为 `last` 时，会连接到上次离开的服务器。
  - 用法：`/remember mode <last, preset>`
  - 默认：`last`


- 设置初始服务器
  - 说明：仅在模式为 `preset` 时生效。
  - 用法：`/remember server <servername>`
  - 默认：`null`（为空则使用代理默认服务器）

也可通过点击命令 `/remember` 或欢迎消息进行设置。

![RememberMe.png][5]

## Optional Features

### Customize your message text

可在配置文件中修改自定义文本，支持 MiniMessage 着色。

可用占位符：`%player%`、`%server%`、`%previousServer%`、`%serverList%`。

### Command-to-broadcast

若不想让某些服的聊天一直全局广播，可开启 “command-to-broadcast”。开启后，仅当消息以 `#` 开头时才会跨服广播。

## Config

**注意事项**

- **自 `v3.2.0` 起自定义文本使用 `MiniMessage` 格式。** 可用 [MiniMessage Viewer](https://webui.adventure.kyori.net/) 预览。

- 若从旧版本升级，配置版本变更时会重建配置文件并重置为默认值。升级前请备份旧配置。

- 可将任意自定义文本设为 `""` 以禁用该条消息。

默认配置如下：

    # essential-playerinfo
    # 配置版本。请勿修改此选项！
    [version]
        version="v3.3"
    
    # 全局 Tab 列表
    [tabList]
        enabled=true
        # `0` 生存，`1` 创造，`2` 冒险，`3` 旁观。
        displayMode=3
    
    # 全局聊天
    [message]
        enabled=true
        command-to-broadcast=false
    
    # Ping 列表
    [pingList]
        enabled=true
    
    # 进服/切换提示
    [connectionTips]
        enabled=true
    
    # 记住我
    [rememberMe]
        enabled=true
    
    # 进服欢迎消息
    [connectMessage]
        enabled=true
        serverName = "示例服务器"
    
    # 自定义消息设置
    [customText]
        enabled = false
        connectionText = "<gray>%player%: 已连接到 <u><hover:show_text:'点击切换服务器。'><click:run_command:'/server %server%'>[%server%]</click></hover></u>。</gray>"
        serverChangeText = "<gray>%player%: <u><click:run_command:'/server %previousServer%'><hover:show_text:'点击切换服务器。'>[%previousServer%]</hover></click></u> -> <u><click:run_command:'/server %server%'><hover:show_text:'点击切换服务器。'>[%server%]</hover></click></u></gray>"
        disconnectionText = "<gray>%player%: 已退出服务器。</gray>"
        chatText = "<gray><u><click:run_command:'/server %server%'><hover:show_text:'点击切换服务器。'>[%server%]</hover></click></u> <%player%> "
        tabListText = "[%server%] %player%"
        connectionMessageText = "<yellow>-------------------------------\n欢迎来到 %serverName%！\n-------------------------------\n你可以点击下方服务器名称切换连接的服务器。\n%serverList%\n-------------------------------\n你可以使用 <u><light_purple><click:run_command:'/remember'><hover:show_text:'点击执行命令'>/remember</hover></click></light_purple></u> 设置默认连接服务器。\n-------------------------------</yellow>"

## To do list

- [x] Get the server list and provide a way to click to switch.

- [x] Add an advance way to customize the messages including some extra feature (By `miniMessage` format).

- [x] Adapt to Velocity 4.x (Java 25, API 4.1.0-SNAPSHOT).

- [ ] Reconfigure the whole plugin before programming next feature.

- [ ] Update the config file and make it more easy to build the custom text.

- [ ] Adapted to message validation for `Minecraft 1.19` or higher.

- [ ] Record the online period time of players.

- [ ] Show the time (or opening time) of server.

## Build

Clone the repository

Open a command prompt/terminal to the repository directory

run `gradlew build`（需要 **JDK 25**）

The built jar file will be in build/libs/

[1]: https://cdn.ussjackdaw.com/image/PingList.png

[2]: https://cdn.ussjackdaw.com/image/TabList2.png

[3]: https://cdn.ussjackdaw.com/image/ConnectMessage1.png

[4]: https://cdn.ussjackdaw.com/image/customText2.png

[5]: https://cdn.ussjackdaw.com/image/RememberMe1.png
