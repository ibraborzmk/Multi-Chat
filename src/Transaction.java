import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Transaction extends Thread implements SocketConnection {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;
    private SynchList outputs;
    private int n;
    private Message message = null;
    protected ArrayList<String> online_users;
    public Transaction(int i, SynchList o, ArrayList _online_users, Socket s) throws Exception {
        this.outputStream = new ObjectOutputStream(s.getOutputStream());
        this.inputStream = new ObjectInputStream(s.getInputStream());
        this.socket = s;
        this.outputs = o;
        this.n = i;
        this.outputs.add(outputStream);
        this.online_users = _online_users;
    }

    public void run() {
        this.communicate();
    }

    @Override
    public void communicate() {

        try {

            while (true) {              
                message = (Message) inputStream.readObject();
                if (message.getName().equals("@join")) {
                    byte[] newUser = message.getMessage();
                    online_users.add(Arrays.toString(newUser));
                    message.setOnlineUsers(online_users);
                    message.setMessage((newUser + " a rejoint la conversation !").getBytes());
                    message.addOnlineUser(Arrays.toString(newUser));
                    message.setName("Server");
                }

                for (int j = 0; j < outputs.size(); j++) {
                    outputs.get(j).writeObject(message);
                    outputs.get(j).flush();
                }
                System.out.println(message.toString());
            }
        } catch (Exception e) {
            this.outputs.remove(outputStream);
        }

        clientLeft(message);

    }

    public void clientLeft(Message m) {
        online_users.remove(m.getName());
        m.setMessage((m.getName() + " a quitt� la conversation!").getBytes());
        m.setToWho("Update");
        m.setName("Server");
        message = m;
        System.out.println(message + " a quitt� la conversation!");
        try {
            for (int j = 0; j < outputs.size(); j++) {
                outputs.get(j).writeObject(message);
                outputs.get(j).flush();
            }
        } catch (Exception e) {
            System.out.println("Error " + e);
            this.outputs.remove(outputStream);
        }
    }
    public void closeConnections() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }
}