import ChatApp.*;          // The package containing our stubs. 
import org.omg.CosNaming.*; // HelloServer will use the naming service. 
import org.omg.CosNaming.NamingContextPackage.*; // ..for exceptions. 
import org.omg.CORBA.*;     // All CORBA applications need these classes. 
import org.omg.PortableServer.*;   
import org.omg.PortableServer.POA;

import java.util.Arrays; 
 import java.util.*;
class ChatImpl extends ChatPOA
{

		ArrayList<String> userTable = new ArrayList<String>();
		ArrayList<ChatCallback> callobjTable = new ArrayList<ChatCallback>(); 

    private ORB orb;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public String say(ChatCallback callobj, String msg)
    {
        callobj.callback(msg);
        return ("         ....Tada!\n");
    }

		public void join(ChatCallback callobj, String username)
		{
			if (userTable.contains(username)) {
				callobj.callback("Error: user " + username + " is already an active chatter"); 
			} else if (callobjTable.contains(callobj)) {
				callobj.callback("Error: you are already online..."); 
			} else {
				userTable.add(username);
				callobjTable.add(callobj);
				callobj.callback("Welcome " + username); 
			}
		}

		public void list(ChatCallback callobj)
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("List of registered users:"); 
			for (int i=0; i < userTable.size(); ++i) 
			{
				stringBuilder.append("\n" + userTable.get(i)); 	
			}
			callobj.callback(stringBuilder.toString());
		}
		
		public void leave(ChatCallback callobj) {
			int pos = callobjTable.indexOf(callobj);
			String username = userTable.get(pos); 
			callobj.callback("Goodbye " + username);
			callobjTable.remove(pos); 
			userTable.remove(pos);
			for (int i=0; i < callobjTable.size(); ++i) 
			{
				ChatCallback reciv = callobjTable.get(i);
				reciv.callback(username + " left the building!");
			}
		}

		public void post(ChatCallback callobj, String msg) {
			int pos = callobjTable.indexOf(callobj);
			String sender = userTable.get(pos);
			for (int i=0; i < callobjTable.size(); ++i) 
			{
				ChatCallback reciv = callobjTable.get(i); 
				reciv.callback(sender + " said: " + msg);
			}
		}
}

public class ChatServer 
{
    public static void main(String args[]) 
    {
	try { 
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null); 

	    // create servant (impl) and register it with the ORB
	    ChatImpl chatImpl = new ChatImpl();
	    chatImpl.setORB(orb); 

	    // get reference to rootpoa & activate the POAManager
	    POA rootpoa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));  
	    rootpoa.the_POAManager().activate(); 

	    // get the root naming context
	    org.omg.CORBA.Object objRef = 
		           orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

	    // obtain object reference from the servant (impl)
	    org.omg.CORBA.Object ref = 
		rootpoa.servant_to_reference(chatImpl);
	    Chat cref = ChatHelper.narrow(ref);

	    // bind the object reference in naming
	    String name = "Chat";
	    NameComponent path[] = ncRef.to_name(name);
	    ncRef.rebind(path, cref);

	    // Application code goes below
	    System.out.println("ChatServer ready and waiting ...");
	    
	    // wait for invocations from clients
	    orb.run();
	}
	    
	catch(Exception e) {
	    System.err.println("ERROR : " + e);
	    e.printStackTrace(System.out);
	}

	System.out.println("ChatServer Exiting ...");
    }

}
