import javax.swing.*;    

public class RouterNode {
  private int nodeID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;

  private int infinity = RouterSimulator.INFINITY;
  private int networkNodes = RouterSimulator.NUM_NODES;               // number of nodes in the array

	private int[] costs = new int[RouterSimulator.NUM_NODES];
  private int[] routes = new int[RouterSimulator.NUM_NODES];
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
      int destID = pkt.destid; 
      int[] mincost = pkt.mincost;
      boolean changed = false; 
      if (nodeID == destID)
      {
        for (int node = 0; node < costs.length; ++node) 
        {
          if (costs[node] != mincost[node]) 
          {
            updateLinkCost(node, mincost[node]);
            changed = true; 
          }
        }
      }
      System.out.println(pkt.sourceid + " -> recvUpdate {destid:" + pkt.destid + "; mincost: [" 
          + pkt.mincost[0] + "," + pkt.mincost[1] + "," + pkt.mincost[2] +"]");

      if (changed) 
      {
        broadcastTable(); 
      }
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
		  myGUI.print("\t" + c);  
	  }
    myGUI.println();

    myGUI.print("routes");
    String out = ""; 
	  for (int r : routes)
    {
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
    System.out.println("node[" +nodeID + "]-> updateLinkCost{" + dest + "=" + newcost + "}");
    costs[dest] = newcost; // set the new cost
    //broadcastTable(); // send a update out
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
