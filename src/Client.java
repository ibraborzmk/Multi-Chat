import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.management.openmbean.InvalidKeyException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class Client implements SocketConnection, ActionListener, ItemListener {

	protected Socket socket;
	protected ObjectInputStream inputStream;
	protected ObjectOutputStream outputStream;
	protected String CurrentToWho = "Everyone";
	protected static ArrayList<String> users_lists;
	private JComboBox<String> display_users;
	private JPanel switchPanels;
	private JFrame window;
	private JPanel cover;
	private JPanel main;
	private JTextField host;
	private JTextField port;
	private JTextField clientName;
	private JButton run;
	private Color clientColour;
	private JLabel labels_cover[];
	private JTextArea server;
	private JTextField input;
	private JScrollPane scrollBar;

	public void runPanel() {

		window = new JFrame("Client");
		labels_cover = new JLabel[6];
		switchPanels = new JPanel(new CardLayout());
		cover = new JPanel();
		cover.setLayout(null);
		cover.setSize(300, 400);
		clientName = new JTextField();
		clientName.setBounds(60, 60, 400, 40);
		clientName.addActionListener(this);
		labels_cover[0] = new JLabel("Nom ou pseudo :");
		labels_cover[0].setBounds(60, 30, 300, 40);
		cover.add(clientName);
		cover.add(labels_cover[0]);
		host = new JTextField();
		host.setBounds(60, 120, 400, 40);
		host.setText("localhost");
		host.addActionListener(this);
		labels_cover[1] = new JLabel("Saisir un hote");
		labels_cover[1].setBounds(60, 90, 300, 40);
		cover.add(labels_cover[1]);
		cover.add(host);
		port = new JTextField();
		port.setBounds(60, 180, 400, 40);
		port.addActionListener(this);
		port.setText("5000");
		labels_cover[2] = new JLabel("entrer le numero de port");
		labels_cover[2].setBounds(60, 150, 300, 40);
		cover.add(labels_cover[2]);
		cover.add(port);

		run = new JButton("Joindre");
		run.setBounds(200, 250, 100, 50);
		run.addActionListener(this);
		cover.add(run);

		switchPanels.add(cover, "cover");

		main = new JPanel();
		main.setLayout(null);
		main.setSize(300, 400);
		mainInterface();
		window.setSize(500, 350);
		switchPanels.add(main, "main");
		window.add(switchPanels);
		window.setResizable(false);
		window.setSize(500, 350);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

	public void mainInterface() {

		main.setBackground(clientColour);

		labels_cover[3] = new JLabel("Chat :");
		labels_cover[3].setForeground(Color.white);
		labels_cover[3].setBounds(30, -5, 300, 40);
		main.add(labels_cover[3]);

		Border thinBorder = LineBorder.createBlackLineBorder();
		server = new JTextArea();
		server.setEditable(false);
		server.setBorder(thinBorder);
		scrollBar = new JScrollPane(server);
		scrollBar.setBounds(20, 30, 450, 150);

		main.add(scrollBar);

		users_lists = new ArrayList<>();
		users_lists.add("Everyone");

		display_users = new JComboBox(users_lists.toArray());
		display_users.setBounds(20, 200, 200, 50);

		display_users.addItemListener(this);

		labels_cover[4] = new JLabel("envoyer a qui?");
		labels_cover[4].setForeground(Color.white);
		labels_cover[4].setBounds(30, 172, 250, 40);
		main.add(labels_cover[4]);
		main.add(display_users);

		labels_cover[5] = new JLabel("Ecrire votre message");
		labels_cover[5].setForeground(Color.white);
		labels_cover[5].setBounds(30, 250, 300, 40);
		main.add(labels_cover[5]);

		input = new JTextField();
		input.setBounds(20, 280, 450, 30);
		input.addActionListener(this);
		main.add(input);

	}

	class serverReader extends Thread {

		public void run() {
			try {
				Message p;
				String message = "";
				while ((p = (Message) inputStream.readObject()) != null) {

					if (p.getToWho().equals("Everyone")) {

						message = message + AESenc.decrypt(p.getMessage(),p.getKey()) + "\n";
						server.setText(message);

					}
					if (p.getToWho().equals("Update")) {

						updateOnlineUsers(p.getOnlineUsers());
						message = message + p.toString() + "\n";
						server.setText(message);
					}

					if (p.getToWho().toLowerCase().equals(clientName.getText().toLowerCase())) {
						message = message +AESenc.decrypt(p.getMessage(),p.getKey()) + "\n";
						server.setText(message);
					}

				}

			} catch (Exception e) {
			}

		}
	}

	public Client() {

		try {

			clientColour = randomColors();
			runPanel();
			int port_number = Integer.parseInt(port.getText());
			socket = new Socket(host.getText(), port_number);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			new serverReader().start();

		} catch (UnknownHostException u) {
			System.out.println(u);
		} catch (IOException i) {
			System.out.println(i);
		}
	}

public void communicate() {
    try {
        String message = input.getText();
		SecretKey Key = AESenc.generateKey();
        byte[] encryptedMessage = AESenc.encrypt(message,Key);


        // Afficher le message original, le message crypté et le message décrypté dans la fenêtre de l'utilisateur
        System.out.println("Original: " + message );
        System.out.println("Crypté: " + encryptedMessage );
		System.err.println("");

		

        // Envoyer le message crypté sur le réseau
        outputStream.writeObject(new Message(this.clientName.getText(),encryptedMessage, CurrentToWho,Key));
        outputStream.flush();
		System.out.print("message envoyer");

    } catch (IOException | InvalidKeyException e) {
        e.printStackTrace();
    } catch (Exception e) {
		throw new RuntimeException(e);
	}
	if (input.getText().toLowerCase().equals("bye")) {
        closeConnections();
        System.exit(0);
    }
    input.setText("");
}


	public void joinUser() throws NoSuchAlgorithmException {
		SecretKey key = AESenc.generateKey();
		try {
			outputStream.writeObject(new Message("@join", AESenc.encrypt(clientName.getText(),key), "Update",key));
			outputStream.flush();

		} catch (IOException i) {
			System.out.println("Error " + i);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		input.setText("");
	}

	public void updateOnlineUsers(ArrayList<String> updated_users) {

		Set<String> set = new HashSet<>(updated_users);
		users_lists.clear();
		users_lists.addAll(set);

		System.out.print(users_lists);
		DefaultComboBoxModel defaultComboBoxModel = new DefaultComboBoxModel(users_lists.toArray());
		display_users.setModel(defaultComboBoxModel);
	}

	public void closeConnections() {
		try {
			inputStream.close();
			outputStream.close();
			this.socket.close();
		} catch (IOException i) {
			System.out.println(i);
		}
	}

	public Color randomColors() {
		Random randomGenerator = new Random();
		int red = randomGenerator.nextInt(256);
		int green = randomGenerator.nextInt(256);
		int blue = randomGenerator.nextInt(256);
		return new Color(red, green, blue);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CardLayout changePages = (CardLayout) (switchPanels.getLayout());
		window.setTitle(this.clientName.getText());
		if (e.getSource() == run && port.getText().length() < 1 && clientName.getText().length() < 1) {
			JOptionPane.showMessageDialog(null, "Tous les formulaires doivent �tre remplis !");
		}
		if (e.getSource() == run && port.getText().length() > 0 && clientName.getText().length() > 0) {
			changePages.show(switchPanels, "main");
			window.setSize(500, 350);
			try {
				joinUser();
			} catch (NoSuchAlgorithmException ex) {
				throw new RuntimeException(ex);
			}
		}
		if (!input.getText().equals("")) {
			communicate();
		}
	}
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			CurrentToWho = (String) e.getItem();
		}
	}





	public static void main(String[] args) {
		new Client();
	}

}
