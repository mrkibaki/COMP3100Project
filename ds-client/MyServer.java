import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(60005);
            Socket s = ss.accept();
            // establish connection

            // BufferedReader dis = new BufferedReader(new
            // InputStreamReader(s.getInputStream()));
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String str;
            // str refers to the message sends from the client

            str = (String) dis.readUTF();//readLine();
            System.out.println("RCVD: " + str);

            dout.write(("G'DAY").getBytes());
            System.out.println("SENT: G'DAY");

            str = (String) dis.readUTF();//readLine();
            System.out.println("RCVD: " + str);

            dout.write(("BYE\n").getBytes());
            System.out.println("SENT: BYE");

            ss.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}