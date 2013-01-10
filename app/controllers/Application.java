package controllers;

import model.ChatRoom;

import org.codehaus.jackson.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.index;

public class Application extends Controller {
  
  public static Result index() {
    return ok(index.render("Your new application is ready."));
  }
  
  public static WebSocket<JsonNode> chat(final String name) {
    return new WebSocket<JsonNode>() {

	@Override
	public void onReady(play.mvc.WebSocket.In<JsonNode> in,
		play.mvc.WebSocket.Out<JsonNode> out) {
	    try{
		ChatRoom.join(name, in, out);
	    } catch(Exception exception) {
		exception.printStackTrace();
	    }
	} 
		
    };
      
  }
}