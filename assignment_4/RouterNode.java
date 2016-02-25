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
    for (int x = 0; x < networkNodes; ++x) {
      if (x != nodeID)
      {  
        for (int z = 0; z < networkNodes; ++z) 
        {
          table[x][z] = (x == z) ? 0 : infinity;  // set to infinity if x == z else set 0
        }
      }
    }
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
    System.out.println("recvUpdate ->" + pkt);
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
    for (int x = 0; x < networkNodes; ++x) 
    {
      myGUI.print(x+"\t"); 
      rowDivider += "===========";
    }
    myGUI.println(); 
    myGUI.println(rowDivider);

    for(int y = 0; y < networkNodes; ++y){
		  if (y != nodeID)
		  {
			  myGUI.print("node " + y + "\t");
			  for (int z = 0; z < networkNodes; ++z){
          String value = Integer.toString(table[y][z]);
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
    costs[dest] = newcost;
    broadcastTable();
  }

  //--------------------------------------------------
  public void broadcastTable() {

    for (int n = 0; n < networkNodes; ++n) 
    {
      if (n != nodeID && costs[n] != infinity) 
      {
        RouterPacket pkt = new RouterPacket(nodeID, n, table[nodeID]);
        sendUpdate(pkt);
      }
    }
  }

}
