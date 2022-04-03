import java.io.*;
import java.net.*;
// import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyClient {
    public Socket s;
    DataOutputStream dout;
    BufferedReader din;
    // CharBuffer buffer;

    public MyClient() {
        try {
            s = new Socket("127.0.0.1", 50000);
            // buffer = CharBuffer.allocate(8192);
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
            // buffer.flip();
            // buffer.toString
            // buffer.clear();
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
            String user = System.getProperty("user.name");
            client.sender("AUTH" + user);
            String firstJob;
            firstJob = client.sender("REDY");
            
            /******************** Getting server list ************************/
            // gets all servers from cluster side and stored in "String list"

            // 这段用来得到服务器的数量
            String serverNumber = client.sender("GETS All");
            int serverNum = client.serverNum(serverNumber);

            // 这段用来得倒服务器清单的String
            String list;
            list = client.serverInfo("OK", serverNum);

            client.sender("OK");
            /************************ Server List ****************************/
            // create a string arraylist (called "serverList")
            // to store all server info needed in
            List<String[]> serverList = new ArrayList<String[]>();// servers

            String[] listLine = list.split("\n");

            // TESTER多少个服务器
            // for (String a : listLine) {
            // System.out.println("可用的服务器： " + a + "\n");
            // }

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

                // System.out.println(serverInfo[0] + serverInfo[1] + serverInfo[2] +
                // "cdsvfdb\n\n");
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
            Iterator<String[]> beginner = utilisedServers.iterator();

            // TESTER!!多少个最大服务器
            // for (String[] a : utilisedServers) {
            // System.out.println("最大服务器: " + a[0] + a[1] + a[2] + "\n");
            // }

            // asign the firstJob
            String splitFirstJob[] = firstJob.split(" ");
            client.sender(
                    "SCHD " + splitFirstJob[2] + " " + utilisedServers.get(0)[0] + " " + utilisedServers.get(0)[1]);

            // assign jobs to utilisedServers
            String getJob = "";
            String jobInfo[];
            String box[] = new String[3];
            while (true) {
                getJob = client.sender("REDY");
                if (getJob.equals("NONE")) {
                    break;
                }
                // System.out.println("this is a message" + getJob);
                // if (jobInfo[0].equals("JCPL")) {
                // System.out.println("this is a test shows error while it doesnt ");
                // // client.sender("REDY");
                // // 这个redy直接删掉，如果jcpl被读到了直接skip这个condition
                // // 可以用if包括JCPL的部分else包括跳过iter的部分
                jobInfo = getJob.split(" ");
                if (!jobInfo[0].equals("JCPL")) {
                    if (iter.hasNext()) {
                        box = iter.next();
                    }
                    client.sender("SCHD " + jobInfo[2] + " " + box[0] + " " + box[1]);

                    if (!iter.hasNext()) {
                        iter = beginner;
                    }
                }
            }

            /*****************************************************************/

            /*************** Largest server (hard coded) ***********************/
            // find the largest server in the list and schedule it "hard-coded".
            // client.sender("SCHD 0 super-silk 0");
            // client.sender("OK");
            /*****************************************************************/

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