import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ClientGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SafeShare - Sender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose a file to send:");
        JButton chooseBtn = new JButton("Browse");
        JLabel fileLabel = new JLabel("No file selected");
        JButton sendBtn = new JButton("Send File");

        final File[] selectedFile = {null};

        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                fileLabel.setText("Selected: " + selectedFile[0].getName());
            }
        });

        sendBtn.addActionListener(e -> {
            if (selectedFile[0] != null) {
                try {
                    SecureFileClient client = new SecureFileClient("127.0.0.1", 5000);
                    client.sendFile(selectedFile[0], "admin", "1234");
                    JOptionPane.showMessageDialog(frame, "✅ File sent successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "❌ Error: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file first.");
            }
        });

        frame.add(label);
        frame.add(chooseBtn);
        frame.add(fileLabel);
        frame.add(sendBtn);
        frame.setVisible(true);
    }
}