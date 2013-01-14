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
    
    //Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();
    Map<String, Session> members = new HashMap<String, Session>(); 
    
    @Override
    public void onReceive(Object message) throws Exception {
	if(message instanceof Join) {
	    Join join = (Join) message;	    
	    if(members.containsKey(join.userId)) {		;    
		this.getSender().tell("이미 다른 브라우저에서 접속 중 입니다. 기존 접속을 끊어주십시오.");
	    } else {
		members.put(join.userId, new Session(join.userId, join.userName, join.location, join.channel));
		this.notifyAll("join", join.userId, join.userName, join.location);
		this.getSender().tell("OK");
	    }
	} else if(message instanceof Talk) {
	    Talk talk = (Talk) message;
	    this.notifyAll("talk", talk.userId, talk.userName, talk.text);
	} else if(message instanceof Quit) {
	    Quit quit = (Quit) message;
	    members.remove(quit.userId);
	    //this.notifyAll("quit", quit.userId, quit.userName, "님이 나가셨습니다.");	    
	} else{
	    this.unhandled(message);
	}
    }
    
    public void notifyAll(String kind, String userId, String userName, String text) {
	for(Session session: members.values()) {
            
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("id", userId);
            event.put("name", userName);
            event.put("message", text);
            
            ArrayNode m = event.putArray("members");

            for(Session s : members.values()) {
        	m.add(s.getName() + "(" + s.getId() + ") - " + s.getLocation());        	
            }
            
            session.getChannel().write(event);            
        }
    }
    
    public static void join(final String userId, final String userName, final String location, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
	String result = (String) Await.result(
		ask(defaultRoom, new Join(userId, userName, location, out), 1000)
		, Duration.create(1,  java.util.concurrent.TimeUnit.SECONDS));
	
	if(result.equals("OK")) {
	    in.onMessage(new Callback<JsonNode>() {
		@Override
		public void invoke(JsonNode event) throws Throwable {
		    defaultRoom.tell(new Talk(userId, userName, event.get("text").asText()));
		}
		
	    });
	    
	    in.onClose(new Callback0() {

		@Override
		public void invoke() throws Throwable {
		   defaultRoom.tell(new Quit(userId, userName));
		}
		
	    });
	} else{
	    ObjectNode error = Json.newObject();
	    error.put("error", result);
	    out.write(error);
	}
	
    }
    
    public static class Join {
	final String userId;
	final String userName;
	final String location;
	final WebSocket.Out<JsonNode> channel;
	
	public Join(String userId, String userName, String location, WebSocket.Out<JsonNode> channel) {
	    this.userId = userId;
	    this.userName = userName;
	    this.location = location;
	    this.channel = channel;
	}
    }
    
    public static class Talk {
        final String userId;
        final String userName;
        final String text;
        
        public Talk(String userId, String userName, String text) {
            this.userId = userId;
            this.userName = userName;
            this.text = text;
        }
        
    }
    
    public static class Quit {
        final String userId;
        final String userName;
        
        public Quit(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
        
    }
}
