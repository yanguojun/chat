/**
 * 
 */
package model;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.ActorRef;
import akka.util.Duration;

/**
 * @author seoi
 *
 */
public class Robot {
    public Robot(ActorRef chatRoom) {
	WebSocket.Out<JsonNode> robotChannel = new WebSocket.Out<JsonNode>() {
	    @Override
	    public void close() { }

	    @Override
	    public void write(JsonNode frame) {
		Logger.of("robot").info(Json.stringify(frame));
	    }	    
	};
	
	chatRoom.tell(new ChatRoom.Join("Robot", robotChannel));
	
	Akka.system().scheduler().schedule(
		Duration.create(30, java.util.concurrent.TimeUnit.SECONDS),
		Duration.create(30, java.util.concurrent.TimeUnit.SECONDS),
		chatRoom,
		new ChatRoom.Talk("Robot", "I'm still alive")
	);
    }
}
