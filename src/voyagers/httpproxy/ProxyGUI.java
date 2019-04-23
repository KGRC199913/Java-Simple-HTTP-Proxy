package voyagers.httpproxy;

import javax.swing.*;

public class ProxyGUI {
    private static JTextArea delgateStatusBoard;
    private JTextArea StatusBoard;
    private JButton startProxyButton;
    private JPanel proxyPanel;
    private JSpinner portSpinner;

    public ProxyGUI() {
        portSpinner.setValue(8888);
        delgateStatusBoard = StatusBoard;
        startProxyButton.addActionListener(e -> {
            int portValue = (int) portSpinner.getValue();
            if ((portValue < 1024) || (portValue > 65535)) {
                StatusBoard.append("Only allow ports in range 1024-65535\n");
                return;
            }
            startProxyButton.setEnabled(false);
            new ProxyController(portValue).start();
            StatusBoard.append("Listening on port " + portValue + "\n");
        });
    }

    public static void logToStatusBoard(String message) {
        delgateStatusBoard.append(message + "\n");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HTTP Proxy Server");
        frame.setContentPane(new ProxyGUI().proxyPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
