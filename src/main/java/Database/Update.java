package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Update {
    public static boolean updatePrice(Connection herokuConn, int id, int price){
        String updateString = "UPDATE orders " +
                "SET price = ? " +
                "WHERE order_id = ? ;";

        try (PreparedStatement ps = herokuConn.prepareStatement(updateString)){
            ps.setInt(1, price);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException sqlE) {
            System.err.format("SQL State: %s\n%s", sqlE.getSQLState(), sqlE.getMessage());
            sqlE.printStackTrace();
        }
            return false;
    }
}
