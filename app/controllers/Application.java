package controllers;

import models.ChatRoom;

import org.codehaus.jackson.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import views.html.*;

public class Application extends Controller {
  
  public static Result index() {
    return ok(index.render("anonymous"));
  }
  
  public static WebSocket<JsonNode> chat(final String id, final String name, final String location) {
    return new WebSocket<JsonNode>() {
	@Override
	public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
	    try{
		ChatRoom.join(id, name, location, in, out);
	    } catch(Exception exception) {
		exception.printStackTrace();
	    }
	} 		
    };      
  }
}