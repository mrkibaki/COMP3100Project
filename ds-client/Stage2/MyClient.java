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

            /******************** BF Algorithm + Optimisation ********************/
            while (true) {
                String getJob = "";
                String jobInfo[];
                String server;
                List<String[]> serverList = new ArrayList<String[]>();
                String listArray[];
                // "serverList" would contain all servers that display by "Gets capable"

                getJob = client.sender("REDY");
                if (getJob.equals("NONE")) {
                    break;
                }
                jobInfo = getJob.split(" ");
                if (!jobInfo[0].equals("JCPL")) {
                    // store server info in String "list" by fucntion "serverInfo"

                    String num;
                    num = client.sender("GETS Capable " + jobInfo[4] + " " + jobInfo[5] + " " + jobInfo[6]);
                    int sNum = client.serverNum(num); // sNum refers to server amount from GETS Capable

                    server = client.serverInfo("OK", sNum);
                    client.sender("OK");

                    listArray = server.split("\n"); // servers in lines
                    for (int i = 0; i < listArray.length; i++) {
                        String eachServer[] = listArray[i].split(" ");
                        // "eachServer" refers to info of single server
                        serverList.add(eachServer);
                    }

                    Iterator<String[]> iter = serverList.iterator();
                    int coreNumberArr[][] = new int[sNum][2];
                    String transferer = "";
                    while (iter.hasNext()) {
                        transferer += iter.next()[4] + " ";
                    }
                    String breaker[] = transferer.split(" ");
                    for (int i = 0; i < coreNumberArr.length; i++) {
                        coreNumberArr[i][0] = Integer.parseInt(breaker[i]);
                        coreNumberArr[i][1] = i;
                    }
                    System.out.println("The selected core number is " + coreNumberArr.length);

                    // say collecting all core info from servers listed in "Gets Capable"
                    // jobInfo[4] contains the number of cores that the job requires for
                    // Hence, a formula can be provided:
                    // "Available cores of a server" - "core number the job requires" = "Number of
                    // cores remaining"
                    // coreNumberArr[i] - jobInfo[4]
                    // The least remaining cores server would be utilised to schdule the specific
                    // job
                    //
                    // optimisation: - Collect all minimum available cores after substraction
                    // and group them to a list.
                    // - Check out the list if there're servers with no waiting job
                    // and cut all others off
                    // - Check the estimated waiting time using "EJWT" in the new list
                    // (iter.remove();) record the server type, id and estimated time
                    // an array.
                    // - find the least estimated time in the array and record the
                    // information. Go through the list again and find the recorded one.
                    // - SCHD job into the recorded server.

                    // =========================== Substraction ============================//
                    // coreNumberArr[i] - jobInfo[4]
                    for (int i = 0; i < coreNumberArr.length; i++) {
                        coreNumberArr[i][0] -= Integer.parseInt(jobInfo[4]);
                        System.out.println("cutted number is " + coreNumberArr[i][0] + " jobInfo " + jobInfo[4]);
                    }

                    // =========================== Find the least one ===========================//
                    int minCore;
                    if (coreNumberArr[0][0] >= 0 || sNum <= 1) {
                        minCore = coreNumberArr[0][0];
                    } else {
                        minCore = 999999999;
                    }
                    int index = 0;
                    for (int i = 0; i < coreNumberArr.length; i++) {
                        if (coreNumberArr[i][0] >= 0 && coreNumberArr[i][0] < minCore) {
                            minCore = coreNumberArr[i][0];
                            index = i;
                        }
                        System.out.println("mincore is " + minCore);
                    }
                    System.out.println("and index is " + index
                            + "; min core is " + minCore);
                    // determine the minimum corein the list

                    iter = serverList.iterator();

                    String serverNeeded[] = new String[9];
                    while (iter.hasNext()) {
                        serverNeeded = iter.next();
                        System.out.println(
                                "server needed " + serverNeeded[0] + " " + serverNeeded[1] + " mincore is " + minCore);
                        if (Integer.parseInt(serverNeeded[4]) == minCore + Integer.parseInt(jobInfo[4])) {
                            System.out.println("This means the loop get into this if condition");
                            break;
                        }

                    }

                    client.sender("SCHD " + jobInfo[2] + " " + serverNeeded[0] + " " + serverNeeded[1]);

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
