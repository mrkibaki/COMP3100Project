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
                    int sNum = client.serverNum(num);
                    // sNum refers to server amount from GETS Capable

                    server = client.serverInfo("OK", sNum);
                    client.sender("OK");

                    listArray = server.split("\n"); // servers in lines
                    for (int i = 0; i < listArray.length; i++) {
                        String eachServer[] = listArray[i].split(" ");
                        // "eachServer" refers to info of single server
                        serverList.add(eachServer);
                    }

                    // "coreNumberArr" indicates a list of server with order number
                    // stored in a 2d array
                    // In this part, only "coreNumberArr" is useful.
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

                    // say collecting all core info from servers listed in "Gets Capable"
                    // jobInfo[4] contains the number of cores that the job requires for
                    // Hence, a formula can be provided:
                    // "Available cores of a server" - "core number the job requires" = "Number of
                    // cores remaining"
                    // coreNumberArr[i] - jobInfo[4]
                    // The least remaining cores server would be utilised to schdule the specific
                    // job
                    //
                    // optimisation:
                    // - Collect all minimum available cores after substraction
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
                    // determine if the first server has positive number of
                    // available cores after substraction.
                    // Else minCore equals a huge number
                    // (if sNum <= 1 then minCore = the only one server)

                    for (int i = 0; i < coreNumberArr.length; i++) {
                        if (coreNumberArr[i][0] >= 0 && coreNumberArr[i][0] < minCore) {
                            minCore = coreNumberArr[i][0];
                        }
                    }
                    System.out.println("minCore is " + minCore);
                    // determine the minimum number of available cores
                    // and store in "minCore"

                    iter = serverList.iterator();

                    // iterator for "serverList"
                    String serverNeeded[] = new String[9];
                    // The String array to pass through to the list and send info out
                    List<String[]> utilisedServer = new ArrayList<String[]>();
                    List<String[]> noWaitingJobServers = new ArrayList<String[]>();
                    while (iter.hasNext()) {
                        serverNeeded = iter.next();

                        /******************************* BF Method *********************************/
                        // if (Integer.parseInt(serverNeeded[4]) == minCore +
                        // Integer.parseInt(jobInfo[4])) {
                        // System.out.println("This means the loop get into this if condition");
                        // break;
                        // }
                        // serverNeeded equals each of the servers' cores,
                        // once find a server with the same number of cores,
                        // serverNeeded = the server and break

                        /******************************* Optimisation *********************************/
                        // 1. find min available cores servers, collect them and store
                        // in "utilisedServer"
                        // 2. client.sender("EJWT " + mediun(serverType) + " " + 1(jobID))
                        // implement above command one by one
                        if (Integer.parseInt(serverNeeded[4]) >= Integer.parseInt(jobInfo[4])) { // minCore +

                            utilisedServer.add(serverNeeded);
                            System.out.println("utilisedServer: " + serverNeeded[0] + " " + serverNeeded[1]);
                        }
                    }

                    Iterator<String[]> utilisedIter = utilisedServer.iterator();

                    int countUt = 0;
                    // determine if there're servers running/no waiting job
                    int waitCount = 0;
                    iter = serverList.iterator();
                    while (iter.hasNext()) {
                        String a[] = iter.next();
                        System.out.println(a[2] + " this means a[2]  " + a[7]);
                        if (Integer.parseInt(a[7]) == 0 && (a[2].equals("idle") || a[2].equals("active"))) {
                            System.out.println(
                                    "THIS REFERS TO THE STATUS OF A[2]: " + a[2] + " status of countUt: " + countUt);
                            noWaitingJobServers.add(a);
                            countUt++;

                        }
                        if (Integer.parseInt(a[7]) != 0) {
                            waitCount++;
                        }
                    }

                    String estimatedTime[] = new String[countUt];
                    String waitEstimatedTime[] = new String[serverList.size()];
                    if (countUt != 0 || waitCount == serverList.size()) {
                        iter = serverList.iterator();
                        String[] bParameter;
                        if (waitCount == serverList.size()) {
                            for (int i = 0; i < serverList.size(); i++) {
                                bParameter = iter.next();
                                waitEstimatedTime[i] = client.sender("EJWT " + bParameter[0] + " " + bParameter[1]);
                                System.out.println(
                                        "eastimated time is " + waitEstimatedTime[i] + "气死我了！！！！");
                            }
                            System.out.println("estimated length is: " + waitEstimatedTime.length);
                            int minTime = Integer.parseInt(waitEstimatedTime[0]);
                            int minServer = 0;
                            for (int i = 0; i < waitEstimatedTime.length; i++) {
                                System.out.println("estimated time is: " + waitEstimatedTime[i]);
                                if (Integer.parseInt(waitEstimatedTime[i]) < minTime) {
                                    minTime = Integer.parseInt(waitEstimatedTime[i]);
                                    minServer++;
                                }
                                System.out.println(minServer + "!!!!!!!");
                            }
                            String realServer[] = new String[9];
                            iter = serverList.iterator();
                            if (minServer == 0) {
                                realServer = iter.next();
                            }
                            while (minServer != 0) {

                                realServer = iter.next();
                                minServer--;
                            }
                            System.out.println("fitted server is: " + realServer[0] + realServer[1] + realServer[2]);
                            client.sender("SCHD " + jobInfo[2] + " " + realServer[0] + " " + realServer[1]);
                        } else {

                            Iterator<String[]> noWaitingIter = noWaitingJobServers.iterator();
                            String parameter[];
                            for (int i = 0; i < estimatedTime.length; i++) {
                                if (noWaitingIter.hasNext()) {
                                    parameter = noWaitingIter.next();
                                    System.out.println("Parameter: " + parameter[0] + " " + parameter[1]);
                                    estimatedTime[i] = client.sender("EJWT " + parameter[0] + " " + parameter[1]);
                                    System.out.println(
                                            "eastimated time is " + estimatedTime[i] + "气死我了！！！！");
                                }
                            }

                            System.out.println(
                                    "eastimated length is " + estimatedTime.length);
                            // find the least estimated time server
                            int minTime = Integer.parseInt(estimatedTime[0]);
                            int minServer = 0;
                            for (int i = 0; i < estimatedTime.length; i++) {
                                System.out.println("estimated time is: " + estimatedTime[i]);
                                if (Integer.parseInt(estimatedTime[i]) < minTime) {
                                    minTime = Integer.parseInt(estimatedTime[i]);
                                    minServer++;
                                }
                                System.out.println(minServer + "!!!!!!!");
                            }

                            String realServer[] = new String[9];
                            noWaitingIter = noWaitingJobServers.iterator();
                            if (minServer == 0) {
                                realServer = noWaitingIter.next();
                            }
                            while (minServer != 0) {

                                realServer = noWaitingIter.next();
                                minServer--;
                            }
                            System.out.println("fitted server is: " + realServer[0] + realServer[1] + realServer[2]);

                            // int count = new int[countUt];
                            // // 初始化指针，然后摘取指针的[0][1]的内容，
                            // // 在loop里发送EJWT信息
                            // // condition是：count.length || utilisedIter.hasNext()
                            // // count[i] = client.sender("EJWT " + utilisedIter[0] + " " +
                            // utilisedIter[1]);
                            // utilisedIter = utilisedServer.iterator();

                            client.sender("SCHD " + jobInfo[2] + " " + realServer[0] + " " + realServer[1]);
                            System.out.println("THIS REFERS TO SHEDULE SUCESSFUL OF OPTIMISATION!");
                        }
                    } else {
                        utilisedIter = utilisedServer.iterator();
                        String[] realServer = new String[9];
                        System.out
                                .println("Starting BF algorithm...");

                        while (utilisedIter.hasNext()) {
                            realServer = utilisedIter.next();
                            System.out.println("realServer: " + realServer[0] + " " + realServer[1]);
                            if (realServer[2].equals("active")) {
                                System.out.println("this means active being implemented");
                                break;
                            }
                        }

                        if (sNum == 1) {
                            iter = serverList.iterator();
                            realServer = iter.next();
                        }

                        System.out.println("printing out the server status: " + realServer[2]);
                        if (!realServer[2].equals("active")) {
                            System.out.println("This means server is not active");
                            utilisedIter = utilisedServer.iterator();
                            realServer = utilisedIter.next();
                        }

                        client.sender("SCHD " + jobInfo[2] + " " + realServer[0] + " " + realServer[1]);
                        System.out.println("this means BF implemented");
                        System.out.println("/************ breaker **************/\n\n");
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
