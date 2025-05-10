import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DatabaseService db = new DatabaseService();
    private static final int MIN_BALANCE = 200;
    private static final int MIN_WITHDRAW = 100;
    private static final int MAX_WITHDRAW = 10_000;
    private static final int MIN_DEPOSIT = 1_000;
    private static final int MAX_DEPOSIT = 500_000;

    public static void main(String[] args) {

        while (true) {
            System.out.println("""
                \nHow may I help you?
                1. Login
                2. Create Mobile Account
                3. Create Savings Account
                4. Exit
                """);

            int choice = promptInt("Enter your choice: ");
            switch (choice) {
                case 1 -> login();
                case 2 -> createMobileAccount();
                case 3-> createSavingsAccount();
                case 4 -> System.exit(0);
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void login(){

        int id = promptInt("Enter your ID: ");
        System.out.print("Enter UserName: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        if (db.authenticateUser(id, username, password)) {
            System.out.println("Login successful");
            userSession(id, username);
        } else {
            System.out.println("Login failed");
        }
    }

    private static void userSession(int id, String username){

        boolean flag = true;

        while(flag) {
            System.out.printf("""
                    \s
                     Good Day, %s. How may I help you?
                    \s
                      1. Check Balance
                      2. Withdraw
                      3. Deposit
                      4. Logout
                    """, username);

            int choice = promptInt("Choose option:");

            switch (choice) {
                case 1 -> System.out.println("Your current Balance is" +
                        ": " + db.getAccountBalance(id));
                case 2 -> withdraw(id);
                case 3 -> deposit(id);
                case 4 -> {
                    flag = false;
                    System.out.println("Exiting. Thank you");
                }
                default -> System.out.println("Enter a valid number!!");
            }
        }
    }

    private static void withdraw(int id){

        int currentBalance = db.getAccountBalance(id);

            int amount = promptInt("Enter amount to withdraw: ");

        if (amount >= MIN_WITHDRAW && amount <= MAX_WITHDRAW &&
                (currentBalance - amount >= MIN_BALANCE)) {
            if (db.updateBalance(id, currentBalance - amount)) {
                System.out.println("Withdrawal successful. New balance: "
                        + db.getAccountBalance(id));
            } else {
                System.out.println("Withdrawal failed.");
            }
        } else {
                System.out.print("Insufficient funds");
            }
    }

    private static void deposit(int id){

        int currentBalance = db.getAccountBalance(id);

        int amount = promptInt("Enter amount to deposit: ");

        if (amount >= MIN_DEPOSIT && amount <= MAX_DEPOSIT) {
            if (db.updateBalance(id, currentBalance + amount)) {
                System.out.println("Deposit successful. New balance: "
                        + db.getAccountBalance(id));
            } else {
                System.out.println("Deposit failed.");
            }
        } else {
            System.out.print("Invalid Amount");
        }
    }

    private static void createSavingsAccount(){

        System.out.print("Enter First Name: ");
        String fname = scanner.nextLine();
        System.out.println("Enter Last Name: ");
        String lname = scanner.nextLine();
        int deposit = promptInt("Enter Initial deposit: ");
        db.createSavingsAccount(new SavingsAccount(fname, lname),deposit);

    }

    private static void createMobileAccount(){

        int id = promptInt("Enter Account Number: ");
        System.out.print("Enter First Name: ");
        String fname = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lname = scanner.nextLine();

        if (!db.accountExists(id, fname, lname)) {
            System.out.println("Account verification failed.");
            return;
        }

        System.out.print("Choose Username: ");
        String username = scanner.nextLine();
        System.out.print("Choose Password: ");
        String password = scanner.nextLine();

        db.createMobileAccount(new MobileAccount(id, username, password));
        System.out.println("Mobile account created successfully.");
    }

    private static int promptInt(String prompt){

        while(true){
            System.out.print(prompt);
            try{
                return Integer.parseInt(scanner.nextLine().trim());
            } catch(NumberFormatException e){
                System.out.println("Invalid Number. Try again");
            }
        }
    }
}
