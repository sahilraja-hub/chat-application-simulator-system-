import java.util.*;
import java.time.LocalDateTime;

public class Main {

    // ================= MESSAGE CLASS =================

    static class Message {
        String sender;
        String receiver;
        String text;
        String timestamp;

        Message(String sender, String receiver, String text) {
            this.sender = sender;
            this.receiver = receiver;
            this.text = text;
            this.timestamp = LocalDateTime.now().toString();
        }

        public String toString() {
            return sender + ": " + text +
                    " [" + timestamp + "]";
        }
    }

    // ================= USER CLASS =================

    static class User {
        String username;
        int unreadCount;

        User(String username) {
            this.username = username;
            this.unreadCount = 0;
        }

        void incrementUnread() {
            unreadCount++;
        }

        void decrementUnread() {
            if (unreadCount > 0) {
                unreadCount--;
            }
        }

        void resetUnread() {
            unreadCount = 0;
        }
    }

    // ================= CHAT SERVICE =================

    static class ChatService {

        // HashMap to store users
        HashMap<String, User> users = new HashMap<>();

        // HashMap to store chat history
        HashMap<String, LinkedList<Message>> chatHistory =
                new HashMap<>();

        // Queue for FIFO message delivery
        Queue<Message> messageQueue = new LinkedList<>();

        // Stack for undo operation
        Stack<Message> undoStack = new Stack<>();

        // Register predefined users
        ChatService() {
            registerUser("Alice");
            registerUser("Bob");
            registerUser("Charlie");
            registerUser("David");
            registerUser("Eve");
        }

        // Register user
        boolean registerUser(String name) {

            name = normalize(name);

            if (name.isEmpty() || users.containsKey(name)) {
                return false;
            }

            users.put(name, new User(name));
            return true;
        }

        // Normalize input
        String normalize(String input) {
            return input == null ? "" : input.trim();
        }

        // Generate common chat key
        String getChatKey(String user1, String user2) {

            if (user1.compareToIgnoreCase(user2) < 0) {
                return user1.toLowerCase() + "_" +
                        user2.toLowerCase();
            }

            return user2.toLowerCase() + "_" +
                    user1.toLowerCase();
        }

        // Send message
        void sendMessage(String from, String to, String text) {

            from = normalize(from);
            to = normalize(to);
            text = normalize(text);

            if (from.isEmpty() || to.isEmpty() || text.isEmpty()) {
                System.out.println("Message cannot be empty.");
                return;
            }

            if (from.equalsIgnoreCase(to)) {
                System.out.println(
                        "Sender and receiver cannot be the same.");
                return;
            }

            if (!users.containsKey(from) ||
                    !users.containsKey(to)) {

                System.out.println("Unknown user.");
                return;
            }

            // Create message
            Message message = new Message(from, to, text);

            // Add message to Queue
            messageQueue.offer(message);

            System.out.println("Message added to Queue.");

            // Process Queue
            processQueue();
        }

        // Process messages using FIFO
        void processQueue() {

            while (!messageQueue.isEmpty()) {

                // Remove first message
                Message message = messageQueue.poll();

                String key = getChatKey(
                        message.sender,
                        message.receiver
                );

                // Create chat history if not available
                chatHistory.putIfAbsent(
                        key, new LinkedList<>()
                );

                // Save message in LinkedList
                chatHistory.get(key).add(message);

                // Push message into Stack
                undoStack.push(message);

                // Increase unread count
                users.get(message.receiver).incrementUnread();

                System.out.println(
                        "Message delivered successfully!"
                );
            }
        }

        // Display chat history
        void showHistory(String user1, String user2) {

            user1 = normalize(user1);
            user2 = normalize(user2);

            if (!users.containsKey(user1) ||
                    !users.containsKey(user2)) {

                System.out.println("Unknown user.");
                return;
            }

            if (user1.equalsIgnoreCase(user2)) {
                System.out.println("Select two different users.");
                return;
            }

            String key = getChatKey(user1, user2);

            LinkedList<Message> history =
                    chatHistory.get(key);

            System.out.println("\n===== CHAT HISTORY =====");

            if (history == null || history.isEmpty()) {
                System.out.println("No messages available.");
                return;
            }

            for (Message message : history) {
                System.out.println(message);
            }

            // Reset unread count for the opened user's inbox
            // Only reset messages received by user1
            for (Message message : history) {
                if (message.receiver.equalsIgnoreCase(user1)) {
                    users.get(user1).decrementUnread();
                }
            }
        }

        // Undo last message
        void undoLastMessage() {

            if (undoStack.isEmpty()) {
                System.out.println("No message to undo.");
                return;
            }

            // Remove latest message from Stack
            Message lastMessage = undoStack.pop();

            String key = getChatKey(
                    lastMessage.sender,
                    lastMessage.receiver
            );

            LinkedList<Message> history =
                    chatHistory.get(key);

            if (history != null &&
                    history.removeLastOccurrence(lastMessage)) {

                users.get(lastMessage.receiver).decrementUnread();

                System.out.println(
                        "Last message undone: " +
                                lastMessage.text
                );

            } else {
                System.out.println("Unable to undo message.");
            }
        }

        // Display all users
        void showUsers() {

            System.out.println("\n===== REGISTERED USERS =====");

            for (User user : users.values()) {

                System.out.println(
                        user.username +
                        " | Unread Messages: " +
                        user.unreadCount
                );
            }
        }
    }

    // ================= MAIN METHOD =================

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ChatService chat = new ChatService();

        int choice;

        System.out.println(
                "=================================="
        );
        System.out.println(
                "     CHAT APPLICATION SIMULATOR"
        );
        System.out.println(
                "=================================="
        );

        do {

            System.out.println("\n========== MENU ==========");
            System.out.println("1. Show Registered Users");
            System.out.println("2. Send Message");
            System.out.println("3. View Chat History");
            System.out.println("4. Undo Last Message");
            System.out.println("5. Register New User");
            System.out.println("6. Exit");

            System.out.print("\nEnter your choice: ");

            if (!sc.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                sc.nextLine();
                continue;
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:

                    chat.showUsers();
                    break;

                case 2:

                    System.out.print("Enter Sender: ");
                    String from = sc.nextLine();

                    System.out.print("Enter Receiver: ");
                    String to = sc.nextLine();

                    System.out.print("Enter Message: ");
                    String text = sc.nextLine();

                    chat.sendMessage(from, to, text);
                    break;

                case 3:

                    System.out.print("Enter First User: ");
                    String user1 = sc.nextLine();

                    System.out.print("Enter Second User: ");
                    String user2 = sc.nextLine();

                    chat.showHistory(user1, user2);
                    break;

                case 4:

                    chat.undoLastMessage();
                    break;

                case 5:

                    System.out.print("Enter New Username: ");
                    String name = sc.nextLine();

                    if (chat.registerUser(name)) {
                        System.out.println(
                                "User registered successfully!"
                        );
                    } else {
                        System.out.println(
                                "Invalid or duplicate username."
                        );
                    }

                    break;

                case 6:

                    System.out.println(
                            "Thank you for using Chat Simulator!"
                    );
                    break;

                default:

                    System.out.println(
                            "Invalid choice. Try again."
                    );
            }

        } while (choice != 6);

        sc.close();
    }
}