/**
 * 
 */
package model;

import static akka.pattern.Patterns.ask;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Await;
import akka.util.Duration;

/**
 * @author seoi
 *
 */
public class ChatRoom extends UntypedActor {

    static ActorRef defaultRoom = Akka.system().actorOf(new Props(ChatRoom.class));
    
    static {
	new Robot(defaultRoom);
    }
    
    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();    
    
    @Override
    public void onReceive(Object message) throws Exception {
	if(message instanceof Join) {
	    Join join = (Join) message;
	    
	    if(members.containsKey(join.userName)) {
		this.getSender().tell("This username is already used");
	    } else {
		members.put(join.userName, join.channel);
		this.notifyAll("join", join.userName, "has entered the room");
		this.getSender().tell("OK");
	    }
	} else if(message instanceof Talk) {
	    Talk talk = (Talk) message;
	    this.notifyAll("talk", talk.userName, talk.text);
	} else if(message instanceof Quit) {
	    Quit quit = (Quit) message;
	    members.remove(quit.userName);
	    this.notifyAll("quit", quit.userName, "has leaved the room");	    
	} else{
	    this.unhandled(message);
	}
    }
    
    public void notifyAll(String kind, String user, String text) {
	for(WebSocket.Out<JsonNode> channel: members.values()) {
            
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);
            
            ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }
            
            channel.write(event);
        }
    }
    
    public static void join(final String userName, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
	String result = (String) Await.result(
		ask(defaultRoom, new Join(userName, out), 1000)
		, Duration.create(1,  java.util.concurrent.TimeUnit.SECONDS));
	
	if(result.equals("OK")) {
	    in.onMessage(new Callback<JsonNode>() {
		@Override
		public void invoke(JsonNode event) throws Throwable {
		    defaultRoom.tell(new Talk(userName, event.get("text").asText()));
		}
		
	    });
	    
	    in.onClose(new Callback0() {

		@Override
		public void invoke() throws Throwable {
		   defaultRoom.tell(new Quit(userName));
		}
		
	    });
	} else{
	    ObjectNode error = Json.newObject();
	    error.put("error", result);
	    out.write(error);
	}
	
    }
    
    public static class Join {
	final String userName;
	final WebSocket.Out<JsonNode> channel;
	
	public Join(String userName, WebSocket.Out<JsonNode> channel) {
	    this.userName = userName;
	    this.channel = channel;
	}
    }
    
    public static class Talk {
        
        final String userName;
        final String text;
        
        public Talk(String userName, String text) {
            this.userName = userName;
            this.text = text;
        }
        
    }
    
    public static class Quit {
        
        final String userName;
        
        public Quit(String userName) {
            this.userName = userName;
        }
        
    }
}
