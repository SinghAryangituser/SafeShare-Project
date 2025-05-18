import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class SecureFileClient {
    private String serverIP;
    private int port;

    public SecureFileClient(String ip, int port) {
        this.serverIP = ip;
        this.port = port;
    }

    public void sendFile(File file, String username, String password) throws Exception {
        Socket socket = new Socket(serverIP, port);

        // Use ObjectOutputStream and ObjectInputStream only
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        // Step 1: Send username/password
        oos.writeObject(username);
        oos.writeObject(password);
        oos.flush();

        // Step 2: Receive auth result
        boolean auth = (boolean) ois.readObject();
        if (!auth) {
            System.out.println("❌ Authentication failed.");
            socket.close();
            return;
        }

        // Step 3: Receive RSA public key
        PublicKey serverPubKey = (PublicKey) ois.readObject();

        // Step 4: Encrypt AES key with RSA and send
        SecretKey aesKey = CryptoUtils.generateAESKey();
        byte[] encryptedAES = CryptoUtils.encryptRSA(aesKey.getEncoded(), serverPubKey);
        oos.writeObject(encryptedAES);

        // Step 5: Encrypt file with AES and send
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] encryptedData = CryptoUtils.encryptAES(fileBytes, aesKey);
        oos.writeObject(file.getName());
        oos.writeObject(encryptedData);

        // Step 6: Send hash
        String hash = HashUtils.getSHA256(fileBytes);
        oos.writeObject(hash);

        System.out.println("✅ File sent successfully.");
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        SecureFileClient client = new SecureFileClient("127.0.0.1", 5000);

        // Replace this with a valid file path
        File fileToSend = new File("C:/Users/ARYAN RAJ/OneDrive/Desktop/sample.txt");


        if (!fileToSend.exists()) {
            System.out.println("❌ File not found.");
            return;
        }

        client.sendFile(fileToSend, "admin", "1234");
    }
}
