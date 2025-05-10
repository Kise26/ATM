import java.util.Scanner;

public class SavingsAccount {

    private String fname;
    private String lname;
    private int initialDeposit;
    private int client_id;
    private int savings_id;

    @Override
    public String toString() {
        return "Client ID: " + client_id +
                "\nName: " + fname + " " + lname +
                "\nSavings ID: " + savings_id +
                "\nBalance: ₱" + initialDeposit;
    }

    public SavingsAccount(String fname, String lname) {
        this.fname = fname;
        this.lname = lname;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public int getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(int initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public int getSavings_id() {
        return savings_id;
    }

    public void setSavings_id(int savings_id) {
        this.savings_id = savings_id;
    }
}
