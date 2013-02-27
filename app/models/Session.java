package models;

import org.codehaus.jackson.JsonNode;

import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;

public class Session extends Member {
        private String location;
        private WebSocket.Out<JsonNode> channel;
        
        public Session(String id, String name, String location,
		Out<JsonNode> channel) {
	    super(id, name);
	    this.location = location;
	    this.channel = channel;
	}
        
	/**
	 * @return the location
	 */
	public String getLocation() {
	    return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
	    this.location = location;
	}
	/**
	 * @return the channel
	 */
	public WebSocket.Out<JsonNode> getChannel() {
	    return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(WebSocket.Out<JsonNode> channel) {
	    this.channel = channel;
	}
}
