package ServernJob;

public class JobInf {
    public int receivedTime; //time job was received
    public String id; //jobid
    public int estRunTime; //estimated job

    public int reqCores; //required cores, memmory and drive space needed by the job
    public int reqMem;
    public int reqDisk; 

    public JobInf(String time, String jobid, String runTime, String cores, String mem, String disk) {
       
       
        this.reqDisk = Integer.valueOf(disk);
        this.reqMem = Integer.valueOf(mem);
        this.reqCores = Integer.valueOf(cores);
        this.receivedTime = Integer.valueOf(time);
        this.id = jobid;
        this.estRunTime = Integer.valueOf(runTime);
        
        
        
    }
}
