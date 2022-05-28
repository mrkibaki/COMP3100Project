import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyClient {
    public Socket s;
    DataOutputStream dout;
    BufferedReader din;

    public MyClient() {
        try {
            s = new Socket("127.0.0.1", 50000);
            dout = new DataOutputStream(s.getOutputStream());
            din = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("opps client wrong");
        }
    }

    public String sender(String msg) {
        String msgPack = "";
        try {
            dout.write((msg + "\n").getBytes());
            dout.flush();
            msgPack = din.readLine();
            System.out.println("RCVD: " + msgPack);
        } catch (IOException e) {
            System.out.println("opps sender wrong");
        }
        return msgPack;
    }

    // split get grab the number of servers from GETS All
    public int serverNum(String msg) {
        int num;
        String info[] = msg.split(" ");
        num = Integer.parseInt(info[1]);
        return num;
    }

    // get number of servers from OK
    public String serverInfo(String msg, int serverNum) {
        String msgPack = "";
        String lineReader = "";
        try {
            dout.write((msg + "\n").getBytes());
            dout.flush();
            for (int i = 0; i < serverNum; i++) {
                lineReader = din.readLine() + "\n";
                System.out.println("RCVD: " + lineReader);
                msgPack += lineReader;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgPack;
    }

    public static void main(String[] args) {

        try {
            // DataInputStream dis = new DataInputStream(s.getInputStream());
            // dispatcher a = new dispatcher(dout, din, "HELO\n", str);

            // form a new client
            MyClient client = new MyClient();

            /************************ Acknowledgement ************************/
            // acknowledgement
            client.sender("HELO");
            String user = System.getProperty("user.name");// get user name from the system
            client.sender("AUTH " + user);
            String firstJob;
            firstJob = client.sender("REDY");

            /******************** Getting server list ************************/
            // gets all servers from cluster side and stored in "String list"

            // store server number in "serverNum" by function "serverNum"
            String serverNumber = client.sender("GETS All");
            int serverNum = client.serverNum(serverNumber);

            // store server info in String "list" by fucntion "serverInfo"
            String list;
            list = client.serverInfo("OK", serverNum);
            client.sender("OK");

            /********************** Server List Array ***********************/
            // create a string arraylist (called "serverList")
            // to store all server info needed in
            List<String[]> serverList = new ArrayList<String[]>();// servers
            String[] listLine = list.split("\n");
            System.out.println(listLine.length);

            // split server info by how many \n among them
            String serverInfo[];
            for (int i = 0; i < listLine.length; i++) {
                // split indexes by how many spaces among them
                serverInfo = new String[3];
                String index[] = listLine[i].split(" ");
                serverInfo[0] = index[0];// server type
                serverInfo[1] = index[1];// server ID
                serverInfo[2] = index[4];// server cores
                serverList.add(serverInfo);// serverList CONTAINS serverInfo[]
            }

            /************************** LRR **********************************/
            // finding the largest servers
            String[] largest = null;
            for (String[] node : serverList) {
                if (largest == null || Integer.parseInt(node[2]) > Integer.parseInt(largest[2])) {
                    largest = node;
                }
            }

            // collect all servers with largest cores in the list, store them in
            // "utilisedServers"
            List<String[]> utilisedServers = new ArrayList<String[]>();
            for (String[] node : serverList) {
                if (Integer.parseInt(node[2]) == Integer.parseInt(largest[2])) {
                    utilisedServers.add(node);
                }
            }

            // Iterator for utilisedServers list
            Iterator<String[]> iter = utilisedServers.iterator();
            // Iterator<String[]> beginner = utilisedServers.iterator();

            // asign the firstJob
            String splitFirstJob[] = firstJob.split(" ");
            client.sender(
                    "SCHD " + splitFirstJob[2] + " " + utilisedServers.get(0)[0] + " " + utilisedServers.get(0)[1]);
            System.out
                    .println("JOB ID " + splitFirstJob[2] + " has been scheduled to " + utilisedServers.get(0)[0] + " "
                            + utilisedServers.get(0)[1] + "\n");

            // assign jobs to utilisedServers
            String getJob = "";
            String jobInfo[];
            String box[] = new String[3];
            if (iter.hasNext()) {
                box = iter.next();
                // Because the firstJob has been processed, first loop shuld start from the
                // second server
            }
            while (true) {
                getJob = client.sender("REDY");
                if (getJob.equals("NONE")) {
                    break;
                }
                jobInfo = getJob.split(" ");
                if (!jobInfo[0].equals("JCPL")) {
                    if (iter.hasNext()) {
                        box = iter.next();
                    }
                    client.sender("SCHD " + jobInfo[2] + " " + box[0] + " " + box[1]);
                    System.out
                            .println("JOB ID " + jobInfo[2] + " has been scheduled to " + box[0] + " " + box[1] + "\n");

                    if (!iter.hasNext()) {
                        iter = utilisedServers.iterator();
                    }
                }
            }

            /*****************************************************************/
            // disconnection
            client.sender("QUIT");

            client.din.close();
            client.dout.close();
            client.s.close();
            /*****************************************************************/

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
