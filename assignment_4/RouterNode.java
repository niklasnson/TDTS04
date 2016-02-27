import javax.swing.*;    

public class RouterNode {
  private int nodeID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;

  private int infinity = RouterSimulator.INFINITY;
  private int networkNodes = RouterSimulator.NUM_NODES;               // number of nodes in the array

	private int[] costs = new int[RouterSimulator.NUM_NODES];           // costs to destination
  private int[] routes = new int[RouterSimulator.NUM_NODES];          // next hop to take
	private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
  
  //------------------------------------------------- constructor
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    nodeID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    for (int node = 0; node < networkNodes; ++node) {
      if (node != nodeID)
      {  
        for (int neighbor = 0; neighbor < networkNodes; ++neighbor) 
        {
          table[node][neighbor] = (node == neighbor) ? 0 : infinity;  // set to infinity if x == z else set 0
        }
      }
    }
    //System.arraycopy(src, srcPos, dest, destPos, length) 
    System.arraycopy(costs, 0, table[nodeID], 0, RouterSimulator.NUM_NODES);
    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    for (int route = 0; route < costs.length; ++route){
    	routes[route] = (costs[route] != infinity) ? route : infinity;
    }

    printDistanceTable();
    broadcastTable();
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
        
            int routeCost = table[nodeID][node]; 
            int directCost = costs[node]; 

            if (directCost < routeCost) 
            {
              table[nodeID][node] = costs[node];
              routes[node] = node; 
              triggerUpdate = true;
            }

            for (int x = 0; x < networkNodes; ++x)
            {
              int ourRouteCost = table[nodeID][x]; 
              int otherRouteCost = table[nodeID][node] + table[node][x];

              if (ourRouteCost < otherRouteCost) 
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
      System.out.println(pkt.sourceid + " -> recvUpdate {destid:" + pkt.destid + "; mincost: [" 
          + pkt.mincost[0] + "," + pkt.mincost[1] + "," + pkt.mincost[2] +"]");

  }  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }
  

  //--------------------------------------------------
  public void printDistanceTable() {
	  myGUI.println("Current table for " + nodeID + "  at time " + sim.getClocktime());

    String rowDivider = ""; 
    myGUI.println();
    myGUI.print("nodes\t");
    for (int node = 0; node < networkNodes; ++node) 
    {
      myGUI.print(node+"\t"); 
      rowDivider += "===========";
    }
    myGUI.println(); 
    myGUI.println(rowDivider);

    for(int node = 0; node < networkNodes; ++node){
		  if (node != nodeID)
		  {
			  myGUI.print("node " + node + "\t");
			  for (int neighbor = 0; neighbor < networkNodes; ++neighbor){
          String value = Integer.toString(table[node][neighbor]);
          if (value.equals("999")) 
          {
            value = "-";
          }
				  myGUI.print(value + "\t");
			  }
			  myGUI.println();
		  }
	  }

    myGUI.println();
    myGUI.print("cost");
    for (int c : costs)
    {
      String out = "";
      if (c == 0) 
      {
        out = "-"; 
      } else {
        out = Integer.toString(c);
      }
		  myGUI.print("\t" + out);  
	  }
    myGUI.println();

    myGUI.print("through node");
	  for (int r : routes)
    {
      String out = ""; 
      if (r == nodeID)
      {
        out = "-";
      } else {
        out = Integer.toString(r);
      }
      myGUI.print("\t" + out);
	  }

    myGUI.println();
    myGUI.println();
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    costs[dest] = newcost; // set the new cost
    if (routes[dest] == dest)
    {
      table[nodeID][dest] = newcost; 
    }

    if (costs[dest] < table[nodeID][dest])
    {
      table[nodeID][dest] = costs[dest]; 
      routes[dest] = dest; 
    }

    for (int node = 0; node < networkNodes; ++node)
    {
      if (table[nodeID][node] > table[nodeID][dest] + table[dest][node])
      {
        table[nodeID][node] = table[nodeID][dest] + table[dest][node];
        routes[node] = routes[dest]; 
      }
    }
    System.out.println("node[" +nodeID + "]-> updateLinkCost{" + dest + "=" + newcost + "}");
    broadcastTable(); // send a update out
  }

  //--------------------------------------------------
  public void broadcastTable() {
    for (int node = 0; node < networkNodes; ++node) 
    {
      // only send to others and if not infinity route
      if (node != nodeID && costs[node] != infinity) 
      {
        //RouterPacket (int sourceID, int destID, int[] mincosts)
        RouterPacket pkt = new RouterPacket(nodeID, node, costs);
        sendUpdate(pkt);
      }
    }
  }

}
