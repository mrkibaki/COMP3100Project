import java.io.*;
import java.net.*;

public class MyClient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("127.0.0.1", 50000);

            // DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            BufferedReader din = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String str;

            dout.write(("HELO\n").getBytes());
            dout.flush();
            System.out.println("SENT: HELO");
            str = din.readLine();
            System.out.println("RCVD: " + str);

            dout.write(("AUTH aaa\n").getBytes());
            dout.flush();
            System.out.println("SENT: AUTH");
            str = din.readLine();
            System.out.println("RCVD: " + str);

            dout.write(("REDY\n").getBytes());
            dout.flush();
            System.out.println("SENT: REDY");
            str = din.readLine();
            System.out.println("RCVD: " + str);

            dout.write(("SCHD 0 super-silk 0\n").getBytes());
            dout.flush();
            System.out.println("SENT: SCHD");
            str = din.readLine();
            System.out.println("RCVD: " + str);

            dout.write(("QUIT\n").getBytes());
            dout.flush();
            System.out.println("SENT: QUIT");
            str = din.readLine();
            System.out.println("RCVD: " + str);

            // dout.writeUTF("BYE");
            // System.out.println("SENT: BYE");

            // str = (String) dis.readUTF();
            // System.out.println("RCVD: " + str);

            din.close();
            dout.close();
            s.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}