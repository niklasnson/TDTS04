import java.util.Arrays;
import javax.swing.*;        

public class RouterNode {

  private GuiTextArea myGUI;
  private RouterSimulator sim;

  private int myID;
  private int infinity = RouterSimulator.INFINITY;
  private int num_network_nodes = RouterSimulator.NUM_NODES;
  private int[] costs = new int[RouterSimulator.NUM_NODES];
	private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
	private int[] route = new int[RouterSimulator.NUM_NODES];
  
  private boolean poisonedReverse = true; 

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");

    System.arraycopy(costs, 0, this.costs, 0, num_network_nodes);

    // Initialize all distance vectors values to infinity
    for (int i=0; i < num_network_nodes; ++i) 
    {
      for (int j=0; j < num_network_nodes; ++j) 
        table[i][j] = infinity;
    }

    // Set this node's distance vector to the direct link cost.
    System.arraycopy(costs, 0, table[myID], 0, num_network_nodes);

    // Initialize the minimal routes to the direct link if exits. 
    for (int i=0; i < num_network_nodes; ++i)
    {
      if (costs[i] != infinity) 
      {
        route[i] = i; 
      } else {
        route[i] = infinity;
      }
    }
    transmit_table(); 
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    System.arraycopy(pkt.mincost, 0, table[pkt.sourceid], 0, num_network_nodes);
		recalculate_table();
  }

  private void recalculate_table() {
    int[] newDistVector = new int[num_network_nodes];

    for (int i=0; i < num_network_nodes; ++i) 
    {
      int path = route[i] = find_shortest_path(i); 
      if (path != infinity) 
      {
        newDistVector[i] = costs[path] + table[path][i];
      } else {
        newDistVector[i] = infinity; 
      }
    }
    
	  if (!Arrays.equals(newDistVector, table[myID])) {
		  table[myID] = newDistVector;
		  transmit_table();
		}    
  }

  private int find_shortest_path(int dest) {
    int distance = costs[dest]; 
    int path; 

    if (distance != infinity) 
      path = dest;
    else 
      path = infinity;

    for (int i=0; i < num_network_nodes; ++i)
    {
      if (i == myID || i == dest)
        continue;

      if (costs[i] != infinity && 
            table[i][dest] != infinity && 
            costs[i] + table[i][dest] < distance) {
            distance = costs[i] + table[i][dest];
            path = i;
          }
    }
    return path; 
  }
  
  public void transmit_table(){
    for (int i=0; i < num_network_nodes; ++i) 
    {
      if (i == myID || costs[i] == infinity)
        continue;
      
      // Create a poisoned distance vector
      int[] distVector = new int[num_network_nodes]; 
      for (int k=0; k < num_network_nodes; ++k) 
      {
        if (poisonedReverse && i == route[k])
        {
          distVector[k] = infinity;
        } else { 
          distVector[k] = table[myID][k];
        }
      }

      // Send the poisoned distance vector 
      RouterPacket pkt = new RouterPacket(myID, i, distVector);
			sendUpdate(pkt);
    }
  }

  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);
  }

  public void printDistanceTable() {
    String rowDivider = "";

    myGUI.println(); 
    myGUI.println();
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());
    myGUI.println();
    myGUI.print(" dst: \t");
    for (int i=0; i < num_network_nodes; ++i) 
    {
      myGUI.print(i + "\t"); 
      rowDivider += "=============";
    }
    myGUI.println();
    myGUI.println(rowDivider);
    for (int source=0; source < num_network_nodes; source++)
    {
      if (source != myID) 
      {
        myGUI.print("nbr" + source + ":\t");
        for (int i=0; num_network_nodes > i; ++i) 
        {
          myGUI.print(table[source][i] + "\t");
        }
        myGUI.println(); 
      }
    }
    myGUI.println();
    myGUI.println(rowDivider);
    
    myGUI.print("cost:"); 
    for (int i=0; num_network_nodes > i; ++i) 
    {
      myGUI.print("\t" + table[myID][i]); 
    }
    
    myGUI.println();

    myGUI.print("route:"); 
    for (int i=0; i < num_network_nodes; ++i)
    {
      if (route[i] != infinity) 
        myGUI.print("\t" + route[i]);
      else
        myGUI.print("\t-");
    }
    myGUI.println();
  }

  public void updateLinkCost(int dest, int newcost) {
    costs[dest] = newcost; 
    recalculate_table(); 
  }
}
