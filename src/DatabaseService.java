import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseService {

    private Connection getConnection(){

        Properties props = new Properties();

        try{
            props.load(Files.newInputStream(Path.of("application.properties"),
                    StandardOpenOption.READ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var dataSource = new MysqlDataSource();

        dataSource.setServerName(props.getProperty("serverName"));
        dataSource.setPort(Integer.parseInt(props.getProperty("port")));
        dataSource.setDatabaseName(props.getProperty("databaseName"));

        try{
            return dataSource.getConnection(
                    System.getenv("MYSQL_USER"),
                    System.getenv("MYSQL_PASS")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean authenticateUser(int id, String userName, String password) {

        String query = "SELECT * FROM clientinfo WHERE client_Id = ?";

        try (
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                int dbId = rs.getInt("client_id");
                String dbpassword = rs.getString("password");
                String dbuserName = rs.getString("userName");

                if (
                        id == dbId &&
                                userName.equalsIgnoreCase(dbuserName) &&
                                password.equalsIgnoreCase(dbpassword)) {

                    System.out.println("LOGIN SUCCESSFUL");
                    return true;
                } else {
                    return false;
                }

            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getAccountBalance(int clientId){

        String query = """
                SELECT savings_amount 
                FROM savingsaccount sa 
                JOIN client c
                ON sa.savings_id = c.savings_id
                WHERE c.client_id = ?""";

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        )
        {
            ps.setInt(1,clientId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("savings_amount") : 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateBalance(int clientId,int amount){

        String query = """
                UPDATE savingsaccount sa
                JOIN client c 
                ON sa.savings_id = c.savings_id
                SET sa.savings_amount = ?
                WHERE c.client_id = ?""";

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ){
            ps.setInt(1,amount);
            ps.setInt(2,clientId);

            return ps.executeUpdate()>0 ;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean accountExists(int clientId, String firstName, String lastName){

        String query = "SELECT * FROM client WHERE (client_Id = ? AND " +
                "firstName = ? AND lastName = ?)";

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ){
            ps.setInt(1,clientId);
            ps.setString(2, firstName);
            ps.setString(3,lastName);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createMobileAccount(MobileAccount mobileAccount){

        String query = "INSERT INTO clientinfo (client_Id, userName, password)" +
                "VALUES (?, ?, ?) ";

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)
        ){
            ps.setInt(1, mobileAccount.getId());
            ps.setString(2, mobileAccount.getUsername());
            ps.setString(3, mobileAccount.getPassword());
            ps.executeUpdate();

            if(ps.getUpdateCount() > 0){
                System.out.println("ACCOUNT SUCCESSFULLY CREATED");
            } else {
                System.out.println("CREATING ACCOUNT FAILED");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createSavingsAccount(SavingsAccount savingsAccount, int amount){

        String query = """
                INSERT INTO client (firstName, lastName)
                VALUES (?,?);
                """;

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ){
            ps.setString(1, savingsAccount.getFname());
            ps.setString(2, savingsAccount.getLname());
            int affectedRows = ps.executeUpdate();

            if(affectedRows > 0){
                try(ResultSet rs = ps.getGeneratedKeys()){
                    if(rs.next()){
                        int generatedClient_id = rs.getInt(1);
                        savingsAccount.setClient_id(generatedClient_id);
                        savingsAccount.setInitialDeposit(amount);
                        initialDeposit(savingsAccount);
                    } else {
                        System.out.println("Account Creation Failed!!");
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initialDeposit(SavingsAccount savingsAccount){

        String query = """
                INSERT INTO savingsaccount (savings_amount)
                VALUES (?)
                """;

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ){
            ps.setInt(1,savingsAccount.getInitialDeposit());
            int affectedRows = ps.executeUpdate();
            if(affectedRows > 0){
                try(
                        ResultSet rs = ps.getGeneratedKeys()
                        ){
                    if(rs.next()){
                        int generatedSavings_id = rs.getInt(1);
                        savingsAccount.setSavings_id(generatedSavings_id);
                        updateClientSavingsID(savingsAccount);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateClientSavingsID(SavingsAccount savingsAccount){

        String query = """
                UPDATE client
                SET savings_id = ?
                WHERE client_id = ?
                """;

        try(
                Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query);
                ){
            ps.setInt(1,savingsAccount.getSavings_id());
            ps.setInt(2,savingsAccount.getClient_id());
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0){
                System.out.println(savingsAccount);;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
