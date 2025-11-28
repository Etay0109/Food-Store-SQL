package org.example;
import java.sql.*;
import java.util.Date;

public class DatabaseConnection {
    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            String dbURL = DBSecrets.getURL();
            String username = DBSecrets.getUsername();
            String pwd = DBSecrets.getPassword();
            return DriverManager.getConnection(dbURL, username, pwd);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("SQL exception : " + ex.getMessage());
            return null;
        }
    }

    public static boolean saveSellerToDB(Statement statement, String sellerName, String sellerPassword){
        try {
            statement.execute("BEGIN");

            String dT = "INSERT INTO details(username, pwd) VALUES ('" + sellerName + "', '" + sellerPassword + "')";
            String sT = "INSERT INTO sellerTable(sellerUserName) VALUES ('" + sellerName + "')";

            statement.executeUpdate(dT); // Add to details table
            statement.executeUpdate(sT); // Add to sellerTable table

            statement.execute("COMMIT");
            return true;

        } catch (SQLException ex) {
            handleSQLException(ex, statement);
            return false;
        }
    }
    public static boolean saveBuyersToDB(Statement statement, String buyerName, String buyerPassword, String buyerCountry, String buyerCity, String buyerBuildingNo, String buyerStreetName){
        try {
            statement.execute("BEGIN");

            String dT = "INSERT INTO details(username, pwd) VALUES ('" + buyerName + "', '" + buyerPassword + "')";
            String bT = "INSERT INTO buyerTable(buyerUserName, country, city, buildingNumber, streetName ) " +
                    "VALUES ('" + buyerName + "', '" + buyerCountry + "', '" + buyerCity + "','" + buyerBuildingNo + "', '" + buyerStreetName + "')";

            statement.executeUpdate(dT); // Add to details table
            statement.executeUpdate(bT); // Add to sellerTable table

            statement.execute("COMMIT");
            return true;

        } catch (SQLException ex) {
            handleSQLException(ex, statement);
            return false;
        }
    }
    public static boolean saveProductsToDB(Statement statement,String productName, String sellerName, String category, double productPrice, double packagePrice ){
        try {
            statement.execute("BEGIN");

            String pT = "INSERT INTO productTable(productName, sellerUserName, category, price) VALUES ('" + productName + "', '" + sellerName + "', '" + category + "', '" + productPrice + "')";
            String spT = "INSERT INTO specialPackaging(extraPrice, productName, sellerUserName) " +
                    "VALUES ('" + packagePrice + "', '" + productName + "', '" + sellerName + "')";

            statement.executeUpdate(pT); // Add to product table
            statement.executeUpdate(spT); // Add to specialPackaging table

            statement.execute("COMMIT");
            return true;
        } catch (SQLException ex) {
            handleSQLException(ex, statement);
        }
        return false;
    }
    public static boolean saveProductForBuyerToDB(Statement statement, Connection conn, String buyerName, String productName, String sellerName){
        /* adding the products that the buyer added to shopping cart to the shoppingCartTable
           and to cartProductTable */
        int cartID;
        double productPrice;
        try {
            statement.execute("BEGIN");
            String sCT;
            productPrice = getPriceFromDB(statement, productName);
            if(!checkIfShoppingCartExists(statement, buyerName)){
                cartID = generateCartIDFromDB(statement);

                sCT = "INSERT INTO shoppingCartTable(cartID, totalPrice, buyerUserName) " +
                        "VALUES (" + cartID + ", " + productPrice + ", '" + buyerName + "')";
            }
            else{
                cartID = getCartIDFromDB(conn, buyerName);

                sCT = "UPDATE shoppingCartTable " +
                        "SET totalPrice = totalPrice + " + productPrice + " " +
                        "WHERE buyerUsername = '" + buyerName + "'";
            }
            statement.executeUpdate(sCT); //create / update shopping cart for buyer

            String cPT = "INSERT INTO cartProductTable(cartID, productName, sellerUserName) VALUES ('" + cartID + "','" + productName + "', '" + sellerName + "')";

            statement.executeUpdate(cPT); //adding products to the cartProductTable

            statement.execute("COMMIT");
            return true;

        } catch (SQLException ex) {
            handleSQLException(ex, statement);
            return false;
        }
    }
    public static boolean saveOrderHistoryToDB(Statement statement, Connection conn, String buyerName){ // saving order history to orderHistoryTable
        try {
            statement.execute("BEGIN");
            int cartID = getCartIDFromDB(conn, buyerName);
            String oHT = "INSERT INTO orderHistoryTable(orderTimestamp, cartID, buyerUsername)" +
                    " VALUES ('" + new Date() + "' , '" + cartID + "', '" + buyerName + "')";
            String sCT = "DELETE FROM shoppingCartTable " +
                    "WHERE buyerUserName = '" + buyerName + "'";
            statement.executeUpdate(oHT); // Adding current shopping cart to orderHistoryTable
            statement.executeUpdate(sCT); // Deleting current shopping cart from shoppingCartTable
            statement.execute("COMMIT");
            return true;

        } catch (SQLException ex) {
            handleSQLException(ex, statement);
            return false;
        }
    }
    public static int generateCartIDFromDB(Statement statement){ // generating Sequence cart ID numbers from the Database
        int cartID = 0;
        try {
            statement.execute("BEGIN");
            String cartIDFromDB = "SELECT nextval('shoppingcarttable_cartid_seq')";
            ResultSet rs = statement.executeQuery(cartIDFromDB);
            if (rs.next()){
                cartID = rs.getInt(1);
            } else {
                throw new SQLException("CartID wasn't generated from database");
            }
            rs.close();
            statement.execute("COMMIT");
        } catch (SQLException ex) {
            handleSQLException(ex, statement);
        }
        return cartID;
    }

    public static boolean deleteShoppingCartFromDB(Statement statement,Connection conn, String buyerName){
        try {
            statement.execute("BEGIN");
            int cartID = getCartIDFromDB(conn,buyerName);
            String cPT = "DELETE FROM cartProductTable " +
                    "WHERE cartID = " + cartID;
            String sCT = "DELETE FROM shoppingCartTable " +
                    "WHERE buyerUserName = '" + buyerName + "'";
            statement.executeUpdate(cPT); // deleting the products in the shopping cart from cartProductTable
            statement.executeUpdate(sCT); // deleting the current shopping cart from shoppingCartTable
            statement.execute("COMMIT");
            return true;

        } catch (SQLException ex) {
            handleSQLException(ex, statement);
            return false;
        }
    }

    public static boolean searchBuyerFromDB(Statement statement, String buyerName){
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT buyerUsername FROM buyerTable " +
                            "WHERE buyerUsername = '" + buyerName + "' ");
            if(rs.next()){
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return false;
    }

    public static boolean searchSellerFromDB(Statement statement, String sellerUsername){
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT sellerUsername FROM sellerTable " +
                            "WHERE sellerUsername = '" + sellerUsername + "' ");
            if (rs.next()){
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return false;
    }

    public static boolean checkIfShoppingCartExists(Statement statement, String buyerName){
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT * FROM shoppingCartTable " +
                            "WHERE buyerUsername = '" + buyerName + "' ");
            if (rs.next()){
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return false;
    }


    public static boolean searchProduct(Statement statement,String productName, String sellerUsername){
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT productName, sellerUsername FROM productTable " +
                            "WHERE sellerUsername = '" + sellerUsername + "' AND productName = '" + productName + "'");
            if (rs.next()){
                rs.close();
                return true;
            }
            rs.close();

        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return false;
    }

    public static boolean checkIfProductIsInSC(Statement statement, String buyerName, String productName){
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT productName FROM cartProductTable  " +
                            "JOIN shoppingCartTable ON cartProductTable.cartID = shoppingCartTable.cartID " +
                            "WHERE shoppingCartTable.buyerUsername = '" + buyerName + "' " +
                            "AND cartProductTable.productName = '" + productName + "'");
            if(rs.next()){
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return false;
    }

    public static boolean printSellerItemsFromDB(Statement statement, String sellerUsername){
        boolean found = false;
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT productName FROM productTable " +
                            "WHERE sellerUsername = '" + sellerUsername + "' ");
            while(rs.next()){
                String productName = rs.getString("productName");
                System.out.println("\t\t -" +productName);
                found = true;
            }
            if(!found){
                System.out.println("No Products found for seller: " + sellerUsername);
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return found;
    }

    public static int getCartIDFromDB(Connection conn, String buyerName){
        int cartID = 0;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT cartID FROM shoppingCartTable " +
                    "WHERE buyerUsername = '" + buyerName + "'");
            if (rs.next()){
                cartID = rs.getInt("cartID");
            }
            rs.close();
            st.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :" + e.getMessage());
        }
        return cartID;
    }


    public static double getTotalPriceFromDB(Connection conn, String buyerName){
        double totalPrice = 0;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT totalPrice FROM shoppingCartTable " +
                    "WHERE buyerUsername = '" + buyerName + "'");
            if (rs.next()){
                totalPrice = rs.getDouble("totalPrice");
            }
            rs.close();
            st.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :" + e.getMessage());
        }
        return totalPrice;
    }


    public static double getPriceFromDB(Statement statement, String productName){
        double price = 0;
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT price FROM productTable " +
                            "WHERE productName = '" + productName + "' ");
            if (rs.next()){
                price = rs.getDouble("price");
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
        return price;
    }

    public static void printBuyersFromDB(Connection conn){
        int i = 1;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT buyerUsername FROM buyerTable");
            while(rs.next()){
                String buyerName = rs.getString("buyerUsername");
                double totalPrice = getTotalPriceFromDB(conn, buyerName);
                System.out.println("buyer " + i + " : ");
                System.out.println("\t name: " + buyerName);
                int cartID = getCartIDFromDB(conn, buyerName);
                System.out.println("\t cart: " + cartID);
                printBuyerSCProducts(conn, buyerName);
                System.out.printf("\t Total: %.2f%n", totalPrice);
                System.out.println("\t history: ");
                printOrderHistoryFromDB(conn, buyerName);
                System.out.println();
                i++;
            }
            rs.close();
            st.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :" + e.getMessage());
        }
    }

    public static void printSellerFromDB(Connection conn, Statement statement){
        int i = 1;
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT sellerUsername FROM sellerTable");
            while(rs.next()){
                String sellerName = rs.getString("sellerUsername");
                System.out.println("seller " + i + " : ");
                System.out.println("\t name: " + sellerName);
                System.out.println("\t items:");
                printSellerItemsFromDB(statement, sellerName);
                System.out.println();
                i++;
            }
            rs.close();
            st.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :" + e.getMessage());
        }
    }

    public static void printItemsByCategory(Statement statement,String category){
        boolean found = false;
        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT * FROM productTable " +
                            "WHERE category = '" + category + "' ");
            while (rs.next()){
                String product = rs.getString("productName");
                System.out.println(" -" + product);
                found = true;
            }
            if(!found){
                System.out.println("There's no products from category " + category);
            }
            rs.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :"  + e.getMessage());
        }
    }
    public static void printOrderHistoryFromDB(Connection conn, String buyerName){
        String lastTimestamp = "";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT orderHistoryTable.orderTimestamp, cartProductTable.productName," +
                    " orderHistoryTable.cartID FROM orderHistoryTable " +
                    "JOIN cartProductTable ON orderHistoryTable.cartID = cartProductTable.cartID " +
                    "WHERE orderHistoryTable.buyerUsername = '" + buyerName + "'");
            while(rs.next()){
                String productName = rs.getString("productName");
                String timestamp = rs.getString("orderTimestamp");
                int cartID = rs.getInt("cartID");
                if(!lastTimestamp.equals(timestamp)){
                    System.out.println("\t" + " cartID: " + cartID + "\t" + timestamp);
                    lastTimestamp = timestamp;
                }
                System.out.println( "\t\t - " + productName);
            }
            rs.close();
            st.close();
        } catch(SQLException e){
            System.out.println("SQL Exception: " + e.getMessage());
        }
    }
    public static void printBuyerSCProducts(Connection conn, String buyerName){
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT productName FROM cartProductTable cp " +
                    "JOIN shoppingCartTable sc ON cp.cartID = sc.cartID " +
                    "WHERE sc.buyerUsername = '" + buyerName + "'");
            while(rs.next()){
                String productName = rs.getString("productName");
                System.out.println("\t - " + productName);
            }
            rs.close();
            st.close();
        } catch (SQLException e){
            System.out.println("SQL Exception :" + e.getMessage());
        }
    }

    public static boolean checkBuyerHistoryFromDB(Statement statement, String buyerName){
        try{
            ResultSet rs = statement.executeQuery("SELECT * FROM orderHistoryTable " +
                    "WHERE buyerUsername = '" + buyerName + "'");
            if(rs.next()){
                rs.close();
                return true;
            }
            rs.close();
        }catch(SQLException e){
            System.out.println("SQL Exception: " + e.getMessage());
        }
        return false;
    }

    public static boolean checkCartIDHistoryTable(Statement statement, int cartID){
        try{
            ResultSet rs = statement.executeQuery("SELECT cartID FROM orderHistoryTable " +
                    "WHERE cartID = " + cartID);
            if(rs.next()){
                rs.close();
                return  true;
            }
            rs.close();
        }catch (SQLException e){
            System.out.println("SQL Exception: " + e.getMessage());
        }
        return false;
    }

    public static double calculateTotalPriceForCart(Connection conn, int cartID) {
        double totalPrice = 0;
        try{
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(     "SELECT SUM(price) AS totalPrice " +
                    "FROM cartProductTable " +
                    "JOIN productTable ON cartProductTable.productName = productTable.productName " +
                    "AND cartProductTable.sellerUsername = productTable.sellerUsername " +
                    "WHERE cartProductTable.cartID = " + cartID);

            if (rs.next()) {
                totalPrice = rs.getDouble("totalPrice");
            }
            statement.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
        }
        return totalPrice;
    }


    public static boolean restoreCartFromDB(Connection conn, int oldCartID, String buyerName){
        Statement writeStatement = null;
        try{
            Statement readStatement = conn.createStatement();
            writeStatement = conn.createStatement();
            int newCartID = generateCartIDFromDB(writeStatement);
            double totalPrice = calculateTotalPriceForCart(conn, oldCartID);
            writeStatement.execute("BEGIN");
            String insertToSC = "INSERT INTO shoppingCartTable(cartID, totalPrice, buyerUsername) " +
                    "VALUES(" + newCartID + ", " + totalPrice + ", '" + buyerName + "')";
            writeStatement.executeUpdate(insertToSC);

            ResultSet rs = readStatement.executeQuery("SELECT productName, sellerUsername FROM cartProductTable " +
                    "WHERE cartID = " + oldCartID);
            while (rs.next()){
                String productName = rs.getString("productName");
                String sellerUsername = rs.getString("sellerUsername");

                String insertProduct = "INSERT INTO cartProductTable(cartID, productName, sellerUsername) " +
                        "VALUES (" + newCartID + ", '" + productName +"', '" + sellerUsername + "')";
                writeStatement.executeUpdate(insertProduct);
            }

            rs.close();
            writeStatement.execute("COMMIT");
            readStatement.close();
            writeStatement.close();

            return true;
        }catch(SQLException e){
            handleSQLException(e, writeStatement);
            return false;
        }
    }

    public static void handleSQLException(SQLException ex, Statement statement) { // function that handles sql exceptions and if there's an exception it will rollback
        System.out.println("SQL Exception: " + ex.getMessage());
        try {
            if (statement != null) {
                statement.execute("ROLLBACK");
                System.out.println("Transaction rolled back.");
            }

        } catch (SQLException rollbackEx) {
            System.out.println("Rollback failed: " + rollbackEx.getMessage());
        }
    }
}
