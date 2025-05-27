import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.File;

public class ClientGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SafeShare - Sender");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 250);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose a file to send:");
        JButton chooseBtn = new JButton("Browse");
        JLabel fileLabel = new JLabel("No file selected");
        JButton sendBtn = new JButton("Send File");

        JProgressBar progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        final File[] selectedFile = {null};

        // File chooser
        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = chooser.getSelectedFile();
                fileLabel.setText("Selected: " + selectedFile[0].getName());
            }
        });

        // Send button with progress
        sendBtn.addActionListener(e -> {
            if (selectedFile[0] != null) {
                new Thread(() -> {
                    try {
                        progressBar.setIndeterminate(true); // Start animation
                        SecureFileClient client = new SecureFileClient("127.0.0.1", 5000);
                        client.sendFile(selectedFile[0], "admin", "1234");

                        SwingUtilities.invokeLater(() -> {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(100);
                            JOptionPane.showMessageDialog(frame, "✅ File sent successfully!");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setIndeterminate(false);
                            JOptionPane.showMessageDialog(frame, "❌ Error: " + ex.getMessage());
                        });
                    }
                }).start();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file first.");
            }
        });

        // Optional: drag-and-drop file support
        frame.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    if (!droppedFiles.isEmpty()) {
                        selectedFile[0] = droppedFiles.get(0);
                        fileLabel.setText("Dropped: " + selectedFile[0].getName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add components to the GUI
        frame.add(label);
        frame.add(chooseBtn);
        frame.add(fileLabel);
        frame.add(sendBtn);
        frame.add(progressBar);
        frame.setVisible(true);
    }
}
