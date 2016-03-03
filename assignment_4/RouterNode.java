import javax.swing.*;    
import java.util.Arrays;
//System.out.println(Arrays.toString(Array));


public class RouterNode {
  private int nodeID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;

  private int infinity = RouterSimulator.INFINITY;
  private int networkNodes = RouterSimulator.NUM_NODES;               // number of nodes in the array

	private int[] costs = new int[RouterSimulator.NUM_NODES];           // costs to destination
  private int[] routes = new int[RouterSimulator.NUM_NODES];          // next hop to take
	private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES]; //This is the table
  private boolean poisonReverse = true;
  private boolean debug = true; 

  //-------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    nodeID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    
    // Construct the table: [0, 4, 1][999, 0, 999][999, 999, 0]
    for (int node = 0; node < networkNodes; ++node) {
      if (node != nodeID)
      {  
        for (int neighbor = 0; neighbor < networkNodes; ++neighbor) 
        {
          // if node is a neighbor set value to 0 else set to infinity. 
          table[node][neighbor] = (node == neighbor) ? 0 : infinity;          
        }
      }
    }

    //System.arraycopy(src, srcPos, dest, destPos, length) 
    System.arraycopy(costs, 0, table[nodeID], 0, RouterSimulator.NUM_NODES);
    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    createRoutesTable();
    
    if (debug) 
    {
	    System.out.println();
      System.out.println("init: nbr" + nodeID); 
    
      System.out.print("[ costs:\t");
      for (int c:costs){
	      System.out.print(c + " ");
	    }
      System.out.printf("]\n");

      System.out.print("[ routes:\t");
	    for (int r:routes){
	      System.out.print(r + " ");
	    }
      System.out.printf("]\n");
      System.out.print("[ table: \t");
	    for (int m:table[nodeID]){
	      System.out.print(m + " ");
	    }
	    System.out.println("]\n");
    }
    printDistanceTable();
    broadcastTable();
  }

  // create the routes table, this contains the next hop to take.  
  private void createRoutesTable()
  {
    for (int route = 0; route < costs.length; ++route){
      int cost = costs[route];
      if (cost < infinity) 
      {
        routes[route] = route;
      }
    }
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
      int source = pkt.sourceid;
      int dest = pkt.destid; 
      int[] mincost = pkt.mincost;
      boolean triggerUpdate = false; 
      boolean neighbourUpdate = false;

      for (int node = 0; node < networkNodes; ++node)
      {
        // If any changes in neighboring nodes table, then save the new costs. 
        if (table[source][node] != mincost[node]) 
        {
          table[source][node] = mincost[node]; 
          neighbourUpdate = true;
        }
      }

      if (neighbourUpdate) 
      {
        for (int node = 0; node < networkNodes; ++node) 
        {
          if (node != nodeID) 
          {
            int oldCost = table[nodeID][node];
            int currentCost = table[routes[node]][node] + table[nodeID][routes[node]];

            if (oldCost != currentCost) 
            {
              table[nodeID][node] = currentCost;
              triggerUpdate = true;
            }
        
            int routeCost = table[nodeID][node];      // cost via route to target.  
            int directCost = costs[node];             // cost directly with target.

            if (directCost < routeCost) 
            {
              table[nodeID][node] = costs[node];
              routes[node] = node; 
              triggerUpdate = true;
            }

            for (int x=0; x < networkNodes; ++x)
            {
              int ourRouteCost = table[nodeID][x]; 
              int otherRouteCost = table[nodeID][node] + table[node][x];

              if (ourRouteCost > otherRouteCost) 
              {
                table[nodeID][x] = otherRouteCost;
                routes[x] = routes[node]; 
                triggerUpdate = true;
              }
            }
          }
        }

        if (triggerUpdate) 
        {
          broadcastTable();
        }
      }
      if (debug) 
      {
        System.out.println("nbr " +pkt.sourceid + " => recvUpdate \t{ destid:" + pkt.destid + "; mincost: [" 
            + pkt.mincost[0] + "," + pkt.mincost[1] + "," + pkt.mincost[2] +"] }");
      }
  }  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }
  
  //--------------------------------------------------
  public void printDistanceTable() {
    myGUI.println(); 
    myGUI.println();
	  myGUI.println("Current table for " + nodeID + "  at time " + sim.getClocktime());

    String rowDivider = ""; 
    myGUI.println();
    myGUI.print(" dst: \t");
    for (int node = 0; node < networkNodes; ++node) 
    {
      myGUI.print(node+"\t"); 
      rowDivider += "=============";
    }
    myGUI.println(); 
    myGUI.println(rowDivider);

    for(int node = 0; node < networkNodes; ++node){
		  if (node != nodeID || false)
		  {
			  myGUI.print("nbr " + node + ":\t");
			  for (int neighbor = 0; neighbor < networkNodes; ++neighbor){
				  myGUI.print(table[node][neighbor] + "\t");
			  }
			  myGUI.println();
		  }
	  }
    
    myGUI.println();
    myGUI.println(rowDivider);
    printCost();  // print the cost
    printRoute(); // print the route
    myGUI.println();
    myGUI.println();
  }
  
  //--------------------------------------------------
  public void printCost()
  {
    myGUI.print("cost:");
    for (int c : costs)
    {
		  myGUI.print("\t" + c);  
	  }
    myGUI.println();
  }

  //--------------------------------------------------
  public void printRoute() 
  {
    myGUI.print("route:");
	  for (int r : routes)
    {
      String out; 
      if (r == 999) 
      {
        out = "\t-";
      } else {
        out = "\t" + r;
      }
      myGUI.print(out);
	  }
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    costs[dest] = newcost;
    
    // if we are routing through the changed link.
    if (routes[dest] == dest)
    {
      table[nodeID][dest] = newcost; 
    }

    // if direct routing is less then through relay. 
    if (costs[dest] < table[nodeID][dest])
    {
      table[nodeID][dest] = costs[dest]; 
      routes[dest] = dest; 
    }

    for (int node = 0; node < networkNodes; ++node)
    {
      if (table[nodeID][node] > table[nodeID][dest] + table[dest][node])
      {
        table[nodeID][node] = (table[nodeID][dest] + table[dest][node]);
        routes[node] = routes[dest]; 
       }
    }
    System.out.println("nbr " + nodeID + " => updateLinkCost \t{ destid: " + dest + "; mincost: " + newcost + " }");
    broadcastTable(); // send a update out
  }

  //--------------------------------------------------
  public void broadcastTable() {
    for (int node = 0; node < networkNodes; ++node) 
    {
      if (node != nodeID && costs[node] != infinity) 
      {
        RouterPacket pkt = new RouterPacket(nodeID, node, costs);        
        if (poisonReverse) 
        {
          int[] posionTable = new int[networkNodes];
          for (int i=0; i < networkNodes; ++i) 
          {
            posionTable[i] = (routes[i] == node) ? infinity : table[nodeID][i];
          }
          pkt = new RouterPacket(nodeID, node, posionTable); 
        }
        sendUpdate(pkt);
      }
    }
  }
}
