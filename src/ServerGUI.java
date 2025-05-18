import javax.swing.*;

public class ServerGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SafeShare - Receiver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);

        JTextArea logArea = new JTextArea(6, 30);
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        JButton startBtn = new JButton("Start Server");

        startBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SecureFileServer.main(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logArea.append("❌ Error: " + ex.getMessage() + "\n");
                }
            }).start();
            startBtn.setEnabled(false);
            logArea.append("✅ Server started. Waiting for connections...\n");
        });

        frame.getContentPane().add(scroll, "Center");
        frame.getContentPane().add(startBtn, "South");
        frame.setVisible(true);
    }
}