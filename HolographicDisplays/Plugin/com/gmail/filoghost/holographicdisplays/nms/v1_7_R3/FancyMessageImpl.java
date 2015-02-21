package com.gmail.filoghost.holographicdisplays.nms.v1_7_R3;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R3.ChatSerializer;
import net.minecraft.server.v1_7_R3.PacketPlayOutChat;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import com.gmail.filoghost.holographicdisplays.nms.interfaces.FancyMessage;

public class FancyMessageImpl implements FancyMessage {
	
	private List<MessagePart> messageParts;
	
	public FancyMessageImpl(String firstPartText) {
		messageParts = new ArrayList<MessagePart>();
		messageParts.add(new MessagePart(firstPartText));
	}
	
	@Override
	public FancyMessageImpl color(ChatColor color) {
		if (!color.isColor()) {
			throw new IllegalArgumentException(color.name() + " is not a color");
		}
		latest().color = color;
		return this;
	}
	
	@Override
	public FancyMessageImpl style(ChatColor... styles) {
		for (ChatColor style : styles) {
			if (!style.isFormat()) {
				throw new IllegalArgumentException(style.name() + " is not a style");
			}
		}
		latest().styles = styles;
		return this;
	}
	
	@Override
	public FancyMessageImpl file(String path) {
		onClick("open_file", path);
		return this;
	}
	
	@Override
	public FancyMessageImpl link(String url) {
		onClick("open_url", url);
		return this;
	}
	
	@Override
	public FancyMessageImpl suggest(String command) {
		onClick("suggest_command", command);
		return this;
	}
	
	@Override
	public FancyMessageImpl command(String command) {
		onClick("run_command", command);
		return this;
	}
	
	@Override
	public FancyMessageImpl tooltip(String text) {
		onHover("show_text", text);
		return this;
	}

	@Override
	public FancyMessageImpl then(Object obj) {
		messageParts.add(new MessagePart(obj.toString()));
		return this;
	}
	
	@Override
	public String toJSONString() {
		StringWriter stringWriter = new StringWriter();
		JsonWriter json = new JsonWriter(stringWriter);
		
		try {
			if (messageParts.size() == 1) {
				latest().writeJson(json);
			} else {
				json.beginObject().name("text").value("").name("extra").beginArray();
				for (MessagePart part : messageParts) {
					part.writeJson(json);
				}
				json.endArray().endObject();
			}
			
		} catch (IOException e) {
			throw new RuntimeException("invalid message");
		}
		return stringWriter.toString();
	}
	
	@Override
	public void send(Player player){
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(toJSONString())));
	}
	
	private MessagePart latest() {
		return messageParts.get(messageParts.size() - 1);
	}
	
	private void onClick(String name, String data) {
		MessagePart latest = latest();
		latest.clickActionName = name;
		latest.clickActionData = data;
	}
	
	private void onHover(String name, String data) {
		MessagePart latest = latest();
		latest.hoverActionName = name;
		latest.hoverActionData = data;
	}
	
}
