import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class SecureFileServer {
    private static final int PORT = 5000;
    private static final String SAVE_DIR = "received_files/";
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "1234";

    public static void main(String[] args) {
        try {
            KeyPair rsaKeyPair = CryptoUtils.generateRSAKeyPair();
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("✅ Server started. Listening on port " + PORT + "...");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("📥 Client connected: " + client.getInetAddress());
                new Thread(() -> handleClient(client, rsaKeyPair)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket, KeyPair rsaKeyPair) {
        try (
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ) {
            // Step 1: Receive credentials
            String username = (String) ois.readObject();
            String password = (String) ois.readObject();
            boolean isAuthenticated = username.equals(VALID_USERNAME) && password.equals(VALID_PASSWORD);

            // Step 2: Send auth result
            oos.writeObject(isAuthenticated);
            oos.flush();

            if (!isAuthenticated) {
                System.out.println("❌ Authentication failed.");
                socket.close();
                return;
            }

            // Step 3: Send RSA public key
            oos.writeObject(rsaKeyPair.getPublic());

            // Step 4: Receive encrypted AES key
            byte[] encryptedAESKey = (byte[]) ois.readObject();
            byte[] aesKeyBytes = CryptoUtils.decryptRSA(encryptedAESKey, rsaKeyPair.getPrivate());
            SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

            // Step 5: Receive encrypted file
            String fileName = (String) ois.readObject();
            byte[] encryptedFile = (byte[]) ois.readObject();
            byte[] fileData = CryptoUtils.decryptAES(encryptedFile, aesKey);

            // Step 6: Receive and verify hash
            String receivedHash = (String) ois.readObject();
            String computedHash = HashUtils.getSHA256(fileData);

            Files.createDirectories(Paths.get(SAVE_DIR));
            Path filePath = Paths.get(SAVE_DIR + "RECEIVED_" + fileName);
            Files.write(filePath, fileData);

            if (receivedHash.equals(computedHash)) {
                System.out.println("✅ File received and hash verified.");
            } else {
                System.out.println("⚠️ WARNING: Hash mismatch! File may be corrupted.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
