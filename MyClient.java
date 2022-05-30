
import java.io.*;
import java.net.*;


import ServernJob.JobInf;
import ServernJob.ServerInf;

public class MyClient {

    private static final int DEFAULT_PORT = 50000;
    

    private static JobInf currentJob; //stores info on the current job that requires scheduling
   
    private static ServerInf[] servers; //stores the server info of the servers

    public static void main(String[] args) {
        try {
            //setting args
            if (args.length == 0) {
                args = new String[1];
                args[0] = "";
            }

            //opening the socket to connect to
            Socket socket = new Socket("localhost", DEFAULT_PORT);

            //initialzing I/O
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            //sending HELO
            sendMessage("HELO", out);

            //getting OK from server
            waitFor("OK", in);

            //sending authentication
            sendMessage("AUTH" + " " + System.getProperty("user.name"), out);

            //getting OK from server
            waitFor("OK", in);

            //sending REDY to start reading jobs
            sendMessage("REDY", out);

            //reading first job
            String msg = receiveMessage(in);
            currentJob = getJobInfo(msg);

            //info array
            servers = getServersData(in, out, "all");

            //making a gap
            System.out.println();

            // schedule first job
            switch (args[0]) {
                default:
                customSchedule(in, out);
                    break;
            }

            //looking for OK
            receiveMessage(in);

            //switching based on what the server reply to REDY
            String command;

            //break loop if no more jobs
            Boolean moreJobs = true;

            //handles messages from the server
            while (moreJobs) {
                //adding a fresh line of space
                System.out.println();
                
                //send REDY for next info
                sendMessage("REDY", out);

                //get reply
                msg = receiveMessage(in);

                //set command to handle reply
                command = msg.substring(0, 4);

                //perform based on server reply
                switch (command) {
                    case "JOBN":
                        //schedule job
                        currentJob = getJobInfo(msg);
            
                        switch (args[0]) {

                            default:
                            customSchedule(in, out);
                                break;

                        }

                        receiveMessage(in);
                        break;

                    case "NONE":
                        //no more jobs, break the loop
                        moreJobs = false;
                        break;

                    

                }
            }

            //quit
            sendMessage("QUIT", out);

            receiveMessage(in);

            out.close();
            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

        //pulls job info in useable format
        private static JobInf getJobInfo(String msg) {

            String[] info = msg.split(" ");
    
            JobInf job = new JobInf(info[1], info[2], info[3], info[4], info[5], info[6]);
    
            return job;
        }
    
        //gets server info
        private static ServerInf getServerInfo(String msg) {
    
            String[] info = msg.split(" ");
    
            ServerInf server = new ServerInf(info[0], info[1], info[4], info[5], info[6]);
            
            return server;
        }
    
            //packages GETS request into a nicer format
    private static ServerInf[] getServersData(BufferedReader in, DataOutputStream out, String mode) {
        String message;
        String reply; 
        //send the correct request
        switch (mode) {
            case "all":
                sendMessage("GETS All", out);
                break;
            case "available":
                sendMessage("GETS Avail " + currentJob.reqCores + " " + currentJob.reqMem + " "
                        + currentJob.reqDisk, out);
                break;
            case "capable":
                sendMessage("GETS Capable " + currentJob.reqCores + " " + currentJob.reqMem + " "
                        + currentJob.reqDisk, out);
                break;
            default:
                return null;
        }

        //get  data
        reply = receiveMessage(in);

        //send OK
        sendMessage("OK", out);

        //check there are servers to return
        int numServers = Integer.valueOf(reply.split(" ")[1]);

        //return if there are no servers matching request
        if (numServers == 0) {
            receiveMessage(in);
            return null;
        }

        //intialise server array
        ServerInf[] servers = new ServerInf[numServers];

        //get server info
        for (int i = 0; i < servers.length; i++) {
            message = receiveMessage(in);
            servers[i] = getServerInfo(message);
        }

        //send OK
        sendMessage("OK", out);

        //get reply
        receiveMessage(in);

        return servers;
    }

  
    //custom algo
    private static void customSchedule(BufferedReader in, DataOutputStream out) {
        try {
            String reply;

            //check for servers with req resources & available
            ServerInf[] availServers = getServersData(in, out, "available");

            //if there are server matching reqs, schedule to first server
            if (availServers != null) {
                //sending scheduling request
                sendMessage("SCHD " + currentJob.id + " " + availServers[0].type + " " + availServers[0].id, out);
            } else {
                //get capable servers that will eventually be available 
                ServerInf[] capServers = getServersData(in, out, "capable");

                //find a capable server with low waiting time
                int index = 0;
                int currentEstWait = 0;
                int minWait = 100000000;
                int scheduleTo = 0;

                
                do {
                    //get wait time for server
                    sendMessage("EJWT " + capServers[index].type + " " + capServers[index].id, out);

                    reply = receiveMessage(in);

                    currentEstWait = Integer.valueOf(reply);

                    //check if estimated wait time is less than current minimum
                    if (currentEstWait < minWait) {
                        minWait = currentEstWait;
                        scheduleTo = index;
                    }

                    index++;
                } while (index < capServers.length);

                //send scheduling message
                sendMessage("SCHD " + currentJob.id + " " + capServers[scheduleTo].type + " "
                            + capServers[scheduleTo].id, out);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
        //reads message and prints it
        private static String receiveMessage(BufferedReader input) {

            try {
    
                String reply = input.readLine();
                
                System.out.println("Received: " + reply);
    
                return reply;
    
            } catch (Exception e) {
                System.out.println(e);
                return null;
            }
        }

    //waits until the specified message is received
    private static void waitFor(String message, BufferedReader in) {
        String input = "";

        try {
            while (!input.equals(message)) {
                input = in.readLine();
            }
            System.out.println("Received: " + input);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //send message to server and prints it
    private static void sendMessage(String message, DataOutputStream output) {
        try {

            output.write((message + "\n").getBytes());
            output.flush();
 
            System.out.println("Sent: " + message);
        } catch (Exception e) {
            System.out.println(e);
        }
    }







    
}