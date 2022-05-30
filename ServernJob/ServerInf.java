package ServernJob;

import java.util.ArrayList;

public class ServerInf {
    public String type; //server type
    public String id; //server id
    public int cores; //cores in the server
    public int memory; //memory in the server
    public int disk; //drive space in the server
    public int jobs; //jobs waiting and running

    public ArrayList<Integer> estCompletTime = new ArrayList<Integer>(); //estimated completion times for current job then following jobs
                                                                            

    public ServerInf(String serverType, String serverID, String serverCores, String serverMemory, String diskIn) {
        this.disk = Integer.valueOf(diskIn);  
        this.memory = Integer.valueOf(serverMemory);
        this.cores = Integer.valueOf(serverCores);
        this.type = serverType;
        this.id = serverID;
        
        estCompletTime.add(0);
    }

    public String toString() {
        return type + " " + id + " " + String.valueOf(cores) + " " + String.valueOf(memory) + " " + String.valueOf(disk)
                + " " + String.valueOf(jobs);
    }
}
