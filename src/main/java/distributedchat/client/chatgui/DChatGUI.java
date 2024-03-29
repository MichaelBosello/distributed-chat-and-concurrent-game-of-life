package distributedchat.client.chatgui;

import distributedchat.utility.NetworkUtility;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class DChatGUI extends JFrame implements ChatGUI {

    private final Set<ChatObserver> guiObserver = new HashSet<>();
    private final JTextArea chat;
    private final JTextField registryHost;
    private final JTextField message;
    private final JButton joinButton;
    private final JButton sendButton;
    private boolean joined = false;

    public DChatGUI() {
        this.setTitle("Distributed Chat");
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (DChatGUI.this.isEnabled()) {
                    notifyLeave();
                    leave();
                }
            }
        });
        this.setLayout(new BorderLayout());

        chat = new JTextArea();
        chat.setEditable(false);
        JScrollPane chatArea = new JScrollPane(chat);
        this.getContentPane().add(chatArea, BorderLayout.CENTER);

        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel("Group registry IP:PORT -> "));
        registryHost = new JTextField(NetworkUtility.ipPortConcat(
                NetworkUtility.getLanOrLocal(), NetworkUtility.CHAT_SERVER_PORT));
        connectionPanel.add(registryHost);
        joinButton = new JButton("Connect");
        joinButton.addActionListener( (e) -> {
            if(!joined) {
                joinButton.setText("Connection");
                joinButton.setEnabled(false);
                registryHost.setEditable(false);
                notifyJoin(registryHost.getText());
            }else{
                notifyLeave();
                leave();
            }
        });
        connectionPanel.add(joinButton);
        this.getContentPane().add(connectionPanel,BorderLayout.PAGE_START);

        JPanel messagePanel = new JPanel();
        message = new JTextField();
        message.setPreferredSize(new Dimension(500, 25));
        messagePanel.add(message);
        sendButton = new JButton("Send");
        sendButton.addActionListener( (e) -> {
            notifySend(message.getText());
            message.setText("");
        });
        sendButton.setEnabled(false);
        messagePanel.add(sendButton);
        this.getContentPane().add(messagePanel,BorderLayout.PAGE_END);

        this.getRootPane().setDefaultButton(sendButton);

        this.setSize(1000, 1000);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void newMessage(String message) {
        chat.append(message + "\n");
    }

    @Override
    public void connected() {
        joined = true;
        joinButton.setText("Leave");
        joinButton.setEnabled(true);
        sendButton.setEnabled(true);
    }

    @Override
    public void connectionError(String error) {
        JOptionPane.showMessageDialog(null, error, "Connection error", JOptionPane.INFORMATION_MESSAGE);
        joinButton.setText("Connect");
        joinButton.setEnabled(true);
        registryHost.setEditable(true);
    }

    @Override
    public void addObserver(ChatObserver observer){
        this.guiObserver.add(observer);
    }

    private void notifyJoin(String host){
        for (final ChatObserver observer : this.guiObserver){
            observer.joinEvent(host);
        }
    }

    private void notifyLeave() {
        for (final ChatObserver observer : this.guiObserver) {
            observer.leaveEvent();
        }
    }

    private void notifySend(String message){
        for (final ChatObserver observer : this.guiObserver){
            observer.sendEvent(message);
        }
    }

    private void leave(){
        JOptionPane.showMessageDialog(null, "Leaving group, please wait", "", JOptionPane.PLAIN_MESSAGE);
        this.setEnabled(false);
    }

}
