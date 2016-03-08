import ChatApp.*;          // The package containing our stubs
import org.omg.CosNaming.*; // HelloClient will use the naming service.
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;     // All CORBA applications need these classes.
import org.omg.PortableServer.*;   
import org.omg.PortableServer.POA;

// -----------------------------------------------------------------------------------
import java.util.Arrays; 
import java.util.Scanner;

class ChatCallbackImpl extends ChatCallbackPOA
{
    private ORB orb;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public void callback(String notification)
    {
        System.out.println(notification);
    }
}

public class ChatClient
{
    static Chat chatImpl;
    
    public static void main(String args[])
    {
	try {
	    // create and initialize the ORB
	    ORB orb = ORB.init(args, null);

	    // create servant (impl) and register it with the ORB
	    ChatCallbackImpl chatCallbackImpl = new ChatCallbackImpl();
	    chatCallbackImpl.setORB(orb);

	    // get reference to RootPOA and activate the POAManager
	    POA rootpoa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
	    
	    // get the root naming context 
	    org.omg.CORBA.Object objRef = 
		orb.resolve_initial_references("NameService");
	    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	    
	    // resolve the object reference in naming
	    String name = "Chat";
	    chatImpl = ChatHelper.narrow(ncRef.resolve_str(name));
	    
	    // obtain callback reference for registration w/ server
	    org.omg.CORBA.Object ref = 
		rootpoa.servant_to_reference(chatCallbackImpl);
	    ChatCallback cref = ChatCallbackHelper.narrow(ref);
	    
	    // Application code goes below


			// Setup connection and wait for a input from the user. 
			
			String usrInput; 
			Scanner in = new Scanner(System.in);
			// Setup the mainloop
			do {
				System.out.print("% "); 
				usrInput = in.nextLine(); 
				execute_command(cref, usrInput);				 
			} while (true); 


	    //System.out.println(chat);
	    
	} catch(Exception e){
	    System.out.println("ERROR : " + e);
	    e.printStackTrace(System.out);
	}
 }

	private static void execute_command(ChatCallback cref, String command)
	{
		String args[] = command.split(" "); 
		if (args[0].equals("list")) { 
			chatImpl.list(cref); 
		} else if (args[0].equals("join") && args.length > 1) {
			chatImpl.join(cref, args[1]);  	
		} else if (args[0].equals("leave")) {
			chatImpl.leave(cref); 
		} else if (args[0].equals("post")) {
			// alla args -> 1 -> slut.join.  
			chatImpl.post(cref, args[0]);
		}
	}
}
