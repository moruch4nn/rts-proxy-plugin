package dev.mr3n.rtsproxy

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import dev.mr3n.vtunnel.VTunnel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor

object DefaultServerCommand {
    fun createDefaultServerCommand(proxy: ProxyServer): BrigadierCommand {
        val command = LiteralArgumentBuilder.literal<CommandSource>("defaultserver")
            .requires { it.hasPermission("rtsproxy.setdefaultserver") }
            .then(
                LiteralArgumentBuilder.literal<CommandSource?>("set")
                    .then(RequiredArgumentBuilder.argument<CommandSource, String>("server", StringArgumentType.string())
                        .suggests { _, builder ->
                            proxy.allServers.map(RegisteredServer::getServerInfo).map(ServerInfo::getName).forEach(builder::suggest)
                            builder.buildFuture()
                        }
                        .executes { ctx ->
                            val server = ctx.getArgument("server", String::class.java)
                            if(proxy.getServer(server).isEmpty) {
                                ctx.source.sendMessage(Component.text("$server というサーバーは存在していません。", Style.style(TextColor.color(254,0,0))))
                                return@executes Command.SINGLE_SUCCESS
                            } else {
                                val old = VTunnel.tryFirst.removeFirstOrNull()
                                VTunnel.tryFirst.add(0, server)
                                ctx.source.sendMessage(Component.text("デフォルトサーバーを $old から $server に変更しました。", Style.style(TextColor.color(0,254,0))))
                                return@executes Command.SINGLE_SUCCESS
                            }
                        })
            ).then(
                LiteralArgumentBuilder.literal<CommandSource?>("get")
                    .executes { ctx ->
                        ctx.source.sendMessage(Component.text("現在のデフォルトサーバーは ${VTunnel.tryFirst.firstOrNull()?:proxy.configuration.attemptConnectionOrder.getOrNull(0)} です。", Style.style(TextColor.color(0,254,0))))
                        return@executes Command.SINGLE_SUCCESS
                    }
            ).build()
        return BrigadierCommand(command)
    }
}