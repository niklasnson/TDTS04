import javax.swing.*;    

public class RouterNode {
  private int nodeID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;

  private int infinity = RouterSimulator.INFINITY;
  private int networkNodes = RouterSimulator.NUM_NODES;               // number of nodes in the array

  private int[] cost = new int[networkNodes];
  private int[] routes = new int[networkNodes];
  private int[][] table = new int[networkNodes][networkNodes];        // multidimensional Arrays
  private int[] costs = new int[RouterSimulator.NUM_NODES];

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    nodeID = ID;
    this.sim = sim;
    
    // [id][id] = $
    // [1][2] = 1;
    // [1][1] = infitity; 
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    for (int x = 0; x < networkNodes; ++x) {
      if (x != nodeID)
      {  
        myGUI.println("-> accessing node for x: " + x + ".");
        for (int z = 0; z < networkNodes; ++z) 
        {
          table[x][z] = (x == z) ? 0 : infinity;  // set to infinity if x == z else set 0
        }
      }
    }
    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);

    // debug: System.out.println(table);
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    System.out.println("recvUpdate ->" + pkt);
  }
  

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    System.out.println("sendUpdate ->" + pkt);
    sim.toLayer2(pkt);
  }
  

  //--------------------------------------------------
  public void printDistanceTable() {

    myGUI.println("Distancetable:");
    myGUI.println("--------------------------------------------------------");
    for (int x = 0; x < networkNodes; ++x) 
    {
      for (int y = 0; y < networkNodes ; ++y) 
      {
        System.out.println(table[x][y]); // prints the value of the position. 
      }
    }
	  myGUI.println("Current table for " + nodeID +
			"  at time " + sim.getClocktime());
  }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    System.out.println("updateLinkCost ->" + dest + "->" + newcost);
    broadcastAll();
  }

  //--------------------------------------------------
  public void broadcastAll() {
      System.out.println("-> -> -> -> -> -> xOx"); 
    // if posion revese is set - send a table with infinity on destinations if we are routing through reciving 
    // node. 
  }

}
