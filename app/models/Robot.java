/**
 * 
 */
package models;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;

/**
 * @author seoi
 *
 */
public class Robot {
    
    public Robot(final ActorRef chatRoom, final String id, final String name, final Set<String> mentions) {
	
	WebSocket.Out<JsonNode> robotChannel = new WebSocket.Out<JsonNode>() {
	    @Override
	    public void close() { }

	    @Override
	    public void write(JsonNode frame) {
		Logger.of("robot").info(Json.stringify(frame));
	    }	    
	};
	
	chatRoom.tell(new ChatRoom.Join(id, name, "", robotChannel), chatRoom);
	
	Akka.system().scheduler().schedule(
		Duration.create(0, TimeUnit.MILLISECONDS),	//초기 딜레이 시간
		Duration.create(15, TimeUnit.SECONDS),		//딜레이 주기
		new Runnable() {
		    @Override
		    public void run() {
			Random random = new Random();
			Object[] mention = mentions.toArray();
			chatRoom.tell(new ChatRoom.Talk(id, name,  (String) mention[random.nextInt(mention.length)]), chatRoom);
		    }
		    
		},
		Akka.system().dispatcher()
	);
    }
}
