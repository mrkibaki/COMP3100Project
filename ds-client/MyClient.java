import java.io.*;
import java.net.*;
import java.util.ArrayList;
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
            System.out.println(e);
        }
    }

    public String sender(String msg) {
        msg = msg + "\n";
        String msgPack = "";
        try {
            dout.write((msg).getBytes());
            dout.flush();
            msgPack = din.readLine();
            System.out.println("RCVD: " + msgPack);
        } catch (IOException e) {
            System.out.println(e);
        }
        return msgPack;
    }

    public index[](){

    }

    public static void main(String[] args) {

        try {
            // DataInputStream dis = new DataInputStream(s.getInputStream());
            // dispatcher a = new dispatcher(dout, din, "HELO\n", str);

            // form a new client
            MyClient client = new MyClient();

            /*****************************************************************/
            // acknowledgement
            client.sender("HELO");
            client.sender("AUTH Kiba");
            client.sender("REDY");
            /*****************************************************************/

            /*****************************************************************/
            // gets all servers from cluster side and stored in "String list"
            String list;
            list = client.sender("GETS All");
            client.sender("OK");

            client.sender("OK");
            /*****************************************************************/

            /*****************************************************************/
            // find the largest server in the list and schedule it "hard-coded".
            client.sender("SCHD 0 super-silk 0");
            client.sender("OK");
            /*****************************************************************/

            client.sender("QUIT");

            client.din.close();
            client.dout.close();
            client.s.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}