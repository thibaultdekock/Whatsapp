package org.example.Form;

import org.example.*;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.Base64;
import java.util.Random;

public class Chat extends JFrame {
    private JTextArea chatArea;
    private RoundedTextField inputField;
    private JButton sendButton;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey secretKey = null;

    private final IBulletinBoard board;
    private String name;
    private boolean initialized = false;
    private int index;
    private String tag;
    private Bump bump;
    private String otherUser;
    private Random random = new Random();

    private final String BUMPFILES_PATH = "./bumpfiles/";

    public Chat() throws Exception {
        InitializeLoginForm();
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        board = (IBulletinBoard) registry.lookup("BulletinBoard");
        assert board != null;

        KeyPair keyPair = Crypto.generateKeys();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();

        // Handshake & get the name
        while (initialized == false) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // clear the form
        getContentPane().removeAll();

        tag = generateTag();
        index = generateIndex();
        // generate Bumpfile
        try(FileWriter fw = new FileWriter(BUMPFILES_PATH+"bmp_"+this.name.toLowerCase() + "$" + ((Integer)(index)).toString() + ".txt")){
            fw.write(Crypto.encodeBumpfile(publicKey, index, tag));
        }

        InitializeWaitForm();
        repaint();
        revalidate();

        // get bumpfile
        bump = Crypto.decryptBumpfile(getBumpFile());

        //derive secretkey using the other's public key
        secretKey = Crypto.deriveKey(secretKey, privateKey, bump.publicKey);

        // clear the form
        getContentPane().removeAll();

        InitializeChatForm();
        repaint();
        revalidate();

        //read incoming messages
        Thread client = new Thread(() -> {
            try {
                while (true){
                    receiveMessage();
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        client.start();
    }

    public void InitializeLoginForm() {
        // Set up the frame
        setTitle("Login to our epic Chat App");
        setSize(350, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(63, 81, 181)); // Blue background color
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title and status
        JLabel title = new JLabel("Enter your first name");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel status = new JLabel("Chat in a secure way", JLabel.LEFT);
        status.setForeground(new Color(200, 255, 200));
        status.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(63, 81, 181));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(status, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // Input Area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 50, 10, 50));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setPreferredSize(new Dimension(100, 70));


        // Rounded send button
        sendButton = new RoundedButton("Connect"); // Text for the button (e.g., an arrow)
        sendButton.setPreferredSize(new Dimension(50, 30));
        //sendButton.setBorder(new EmptyBorder(100, 10, 10, 10));
        // add margin to the button
        sendButton.setMargin(new Insets(10, 10, 10, 10));

        // Input field with placeholder
        inputField = new RoundedTextField("Type your name here...");
        inputField.setPreferredSize(new Dimension(100, 30));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding for input field


        inputPanel.add(inputField, BorderLayout.NORTH);
        inputPanel.add(sendButton, BorderLayout.CENTER);
        // fill the space under the input field
        inputPanel.add(Box.createRigidArea(new Dimension(0, 50)), BorderLayout.SOUTH); // Spacer between input and button


        // Add action for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initUser();
            }
        });

        // Allow "Enter" key to send messages
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initUser();
            }
        });

        // Add components to the main frame
        add(headerPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);

        setVisible(true);
    }
    public void InitializeWaitForm(){
        // Set up the frame
        setTitle("Chat App");
        setSize(350, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(63, 81, 181)); // Blue background color
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title and status
        JLabel title = new JLabel("Welcome, " + this.name + "!");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel status = new JLabel("We wait for a connection to be made..", JLabel.LEFT);
        status.setForeground(new Color(200, 255, 200));
        status.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(63, 81, 181));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(status, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // Add components to the main frame
        add(headerPanel, BorderLayout.NORTH);
        //add(scrollPane, BorderLayout.CENTER);
        //add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void InitializeChatForm() {
        // Set up the frame
        setTitle("Chat App");
        setSize(350, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(63, 81, 181)); // Blue background color
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title and status
        JLabel title = new JLabel(this.name);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel status = new JLabel("This time, we'll reply in a secure way..", JLabel.LEFT);
        status.setForeground(new Color(200, 255, 200));
        status.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(63, 81, 181));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(status, BorderLayout.SOUTH);

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        // Chat Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Hide horizontal scrollbar
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);   // Hide vertical scrollbar
        scrollPane.setBorder(null);

        // DocumentListener to auto-scroll
        chatArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                scrollToBottom();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                scrollToBottom();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                scrollToBottom();
            }

            private void scrollToBottom() {
                SwingUtilities.invokeLater(() -> {
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            }
        });

        // Input Area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(Color.WHITE);


        // Rounded send button
        sendButton = new RoundedButton("➤"); // Text for the button (e.g., an arrow)
        sendButton.setPreferredSize(new Dimension(50, 30));
        //sendButton.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Input field with placeholder
        inputField = new RoundedTextField("Type your message here...");
        inputField.setPreferredSize(new Dimension(260, 30));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Inner padding for input field

        inputPanel.add(inputField, BorderLayout.WEST);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.CENTER); // Spacer between input and button
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add action for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Add listener that scrolls down the chat area when new messages are added


        // Allow "Enter" key to send messages
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Add components to the main frame
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initUser(){
        String message = inputField.getText().trim();
        if (!message.isEmpty() && !message.equals(((RoundedTextField) inputField).getPlaceholder())) {
            this.name = message;
            inputField.setText("");
            inputField.requestFocus();
            // Placeholder for secure message sending logic
            initialized = true;
        }
    }
    private void sendMessage() throws Exception {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && !message.equals(((RoundedTextField) inputField).getPlaceholder())) {
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
            inputField.requestFocus();

            // Placeholder for secure message sending logic
            int nextIndex = generateIndex();
            String nextTag = generateTag();
            Message msg = new Message(message, nextIndex, nextTag);
            String encryptedMsg = msg.encrypt(secretKey);

            String challenge = board.requestChallenge();
            PoWSolver solver = new PoWSolver();
            String nonce = solver.solveChallenge(challenge, IBulletinBoard.POW_DIFFICULTY);

            board.add(index, encryptedMsg, tag, nonce, challenge);
            System.out.printf("%s sent msg: %s%n", name, message);
            System.out.println(msg);
            index = nextIndex;
            tag = nextTag;
            secretKey = Crypto.deriveKey(secretKey, privateKey, bump.publicKey);
        }
    }

    private void receiveMessage() throws Exception {
        String msg = board.get(bump.index, bump.tag);
        while (msg != null){
            //Parse msg
            Message decryptedMsg = Message.decrypt(msg, secretKey);
            chatArea.append(String.format("%s: %s%n", otherUser, decryptedMsg.message));

            bump.tag = decryptedMsg.tag;
            bump.index = decryptedMsg.index;
            secretKey = Crypto.deriveKey(secretKey, privateKey, bump.publicKey);

            // Check for more messages
            msg = board.get(bump.index, bump.tag);
        }
    }


    // Custom JTextField with rounded corners and placeholder text
    static class RoundedTextField extends JTextField {
        private final String placeholder;

        public RoundedTextField(String placeholder) {
            this.placeholder = placeholder;
            setBorder(new EmptyBorder(5, 10, 5, 10));
            setFont(new Font("Arial", Font.PLAIN, 14));
            setForeground(Color.GRAY);
            setText(placeholder);

            // Add focus listener for placeholder text
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }


        public String getPlaceholder() {
            return placeholder;
        }

        @Override
        protected void paintBorder(Graphics g) {
            g.setColor(new Color(220, 220, 220));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (isOpaque()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set the background color and fill the rounded rectangle
                g2.setColor(new Color(222, 1, 1)); // Background color (same as your original background color)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded corners

                g2.dispose();
            }
            super.paintComponent(g); // Ensure the text and cursor are drawn properly
        }
    }

    public class RoundedButton extends JButton {

        public RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set button color
            g2.setColor(new Color(63, 81, 181)); // Customize with your preferred color
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            setForeground(Color.WHITE); // Text color

            // Draw button text
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
            super.paintComponent(g);
        }


        @Override
        public Dimension getPreferredSize() {
            return new Dimension(50, 30); // Adjust to your desired size
        }
    }

    private File getBumpFile() throws Exception {
        while (true){
            for (File file: new File(BUMPFILES_PATH).listFiles()) {
                String fileName = file.getName();
                String username = fileName.substring(4, fileName.indexOf("$"));
                int id = Integer.parseInt(fileName.substring(fileName.indexOf("$")+1, fileName.indexOf(".txt")));
                if(!username.equals(name.toLowerCase())){
                    otherUser = username;
                    return file;
                } else if(username.equals(name.toLowerCase()) && id!=index){
                    otherUser = username;
                    return file;
                }
            }
            Thread.sleep(1000);
        }
    }
    private String generateTag() {
        StringBuilder stringBuilder = new StringBuilder(50);
        for (int i = 0; i < 50; i++) {
            stringBuilder.append((char)random.nextInt(33, 126));
        }
        return stringBuilder.toString();
    }
    private int generateIndex() throws Exception {
        return random.nextInt(0, board.getSize());
    }

}