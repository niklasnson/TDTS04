package ChatApp;


/**
* ChatApp/ChatOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Chat.idl
* Wednesday, 9 March 2016 10:03:51 o'clock CET
*/

public interface ChatOperations 
{
  String say (ChatApp.ChatCallback objref, String message);
  void join (ChatApp.ChatCallback objref, String username);
  void list (ChatApp.ChatCallback objref);
  void leave (ChatApp.ChatCallback objref);
  void post (ChatApp.ChatCallback objref, String msg);
  void game (ChatApp.ChatCallback objref, String data);
} // interface ChatOperations
