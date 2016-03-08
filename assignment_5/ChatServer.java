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
		ArrayList<ChatCallback> teamEnlighten = new ArrayList<ChatCallback>(); 
		ArrayList<ChatCallback> teamResistance = new ArrayList<ChatCallback>(); 
	 	String [][] gameTable = new String[9][9];

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

		public void quit(ChatCallback callobj) {
		}

		public void game(ChatCallback callobj, String data) {
			String args[] = data.split(" "); 
			if (args[0].equals("join") && args.length > 1) {
				assignTeam(callobj, args[1]);
			} else if (args[0].equals("hack") && args.length > 1 ) {
				int x = Integer.parseInt(args[1]);
				int y = Integer.parseInt(args[2]); 
				if (x > -1 && x < 9 && y > -1 && y < 9 && gameTable[x][y] == null)
				{
						gameTable[x][y] = getSymbol(callobj);
				}
				renderTable();
			}
		}

		private boolean hasWon() {
 			return false; 	
		}

		private void assignTeam(ChatCallback callobj, String team) {
			boolean success = false; 
			if (team.equals("enl"))  {
				teamEnlighten.add(callobj);
				callobj.callback("You joined the enlightend!"); 
			} else if (team.equals("res")) {
				teamResistance.add(callobj);
				callobj.callback("You joined the resistance!");
			}
		}

		private String getSymbol(ChatCallback callobj) {
			if (teamResistance.contains(callobj))
				return "R";
			else if (teamEnlighten.contains(callobj)) 
				return "E";
			else
				return "*"; 

		}

		private void renderTable() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("\n"); 
			for (int y=0; y < 9; ++y) 
			{
				for (int x=0; x < 9; ++x) 
				{
					if (gameTable[y][x] == null) 
						stringBuilder.append("* ");
					else
						stringBuilder.append(gameTable[y][x] + " ");
				}
				stringBuilder.append("\n");
			}
			if (hasWon()) {
				stringBuilder.append("We have a winner!"); 
			}

			for (int i=0; i < teamResistance.size(); ++i)
			{
				ChatCallback reciv = teamResistance.get(i); 
				reciv.callback(stringBuilder.toString());
			}

			for (int i=0; i < teamEnlighten.size(); ++i) 
			{
				ChatCallback reciv = teamEnlighten.get(i); 
				reciv.callback(stringBuilder.toString());
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
