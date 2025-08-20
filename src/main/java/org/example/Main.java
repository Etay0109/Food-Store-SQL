package org.example;

import java.util.*;
import java.sql.*;


public class Main {
    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        Connection conn = DatabaseConnection.connect();
        if (conn != null)
            System.out.println("Connection established successfully");
        else {
            System.out.println("Connection failed");
            return;
        }
        int choice;
        boolean fContinue = true;
        Statement statement;
        try {
            statement = conn.createStatement(); //create an interaction with the DB
        } catch (SQLException e) {
            System.out.println("Error occurred, please try again");
            return;
        }
        do {
            try {
                System.out.println("Choose one of the following options: ");
                System.out.println("0 - For Exit");
                System.out.println("1 - Add Seller");
                System.out.println("2 - Add Buyer");
                System.out.println("3 - Add Product For Seller");
                System.out.println("4 - Add Product For Buyer");
                System.out.println("5 - Payment For Buyer");
                System.out.println("6 - Buyer information");
                System.out.println("7 - Seller information");
                System.out.println("8 - Display products by category");
                System.out.println("9 - Create a new cart from history");
                System.out.println("Please enter your choice -->");
                choice = s.nextInt();
                s.nextLine();
                switch (choice) {
                    case 1:
                        AddSeller(s,statement);
                        break;
                    case 2:
                        AddBuyer(s, statement);
                        break;
                    case 3:
                        AddProductForSeller(s, statement);
                        break;
                    case 4:
                        AddProductForBuyer(s, conn, statement);
                        break;
                    case 5:
                        PaymentForBuyer(s, statement, conn);
                        break;
                    case 6:
                        BuyerInformation(conn);
                        break;
                    case 7:
                        SellerInformation(conn, statement);
                        break;
                    case 8:
                        displayProductsByCategory(s, statement);
                        break;
                    case 9:
                        createCartFromHistory(s, conn, statement);
                        break;
                    case 0:
                        fContinue = false;
                        break;
                    default:
                        System.out.println("Invalid choice");
                        break;
                }
                System.out.println();
            } catch (EmptyCartException e) {
                System.out.println("Cart is empty. Cannot do payment!");
            } catch (Exception e) {
                System.out.println("Error occurred. Please try again!" + e.getMessage());
            }
        } while (fContinue);

        System.out.println("Thank you for using this system");
    }


    private static void AddSeller(Scanner s, Statement statement) {  // add seller - option 1
        String sellerName = Util.captureString(s, "Enter Seller's username: \n");

        if (DatabaseConnection.searchSellerFromDB(statement, sellerName)) {
            System.out.println("Seller already exists");
            return;
        }

        String sellerPassword = Util.captureString(s, "Enter Seller's password: \n");

        if (!DatabaseConnection.saveSellerToDB(statement, sellerName, sellerPassword)) {
            return;
        }
        System.out.println("The seller's username added successfully");
    }

    private static void AddBuyer(Scanner s, Statement statement) { // add buyer - option 2
        String buyerName = Util.captureString(s, "Enter buyer's username: \n");
        if (DatabaseConnection.searchBuyerFromDB(statement, buyerName)) {
            System.out.println("Username already exists");
            return;
        }
        String buyerPassword = Util.captureString(s, "Enter buyer's password: \n");
        String buyerStreetName = Util.captureString(s, "Enter buyer's streetName: \n");
        String buyerBuildingNo = Util.captureString(s, "Enter buyer's buildingNo: \n");
        String buyerCity = Util.captureString(s, "Enter buyer's city: \n");
        String buyerCountry = Util.captureString(s, "Enter buyer's country: \n");

        if (!DatabaseConnection.saveBuyersToDB(statement, buyerName, buyerPassword, buyerCountry, buyerCity, buyerBuildingNo, buyerStreetName)) {
            return;
        }
        System.out.println("The buyer's username added successfully");
    }

    private static void AddProductForSeller(Scanner s, Statement statement) { // add product for seller - option 3
        String sellerName = Util.captureString(s, "Please enter seller's name \n");

        try {
            ResultSet rs = statement.executeQuery(
                    "SELECT sellerUsername FROM sellerTable " +
                            "WHERE sellerUsername = '" + sellerName + "' ");
            if (!rs.next()) {
                System.out.println("Seller not found ");
                return;
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception :" + e.getMessage());
        }

        String productName = Util.captureString(s, "Please enter the product name: \n");
        double productPrice = Util.captureDouble(s, "Please enter product price: \n");
        int productCategory = Util.captureIntWithRange(s, List.of("Please select number for product category", "1 :children", "2 :electronics", "3 :office", "4 :clothing"), 1, 4);

        String category;

        if (productCategory == 1) {
            category = "children";
        } else if (productCategory == 2) {
            category = "electronics";
        } else if (productCategory == 3) {
            category = "office";
        } else if (productCategory == 4) {
            category = "clothing";
        } else {
            System.out.println("Invalid category.");
            return;
        }

        double packagePrice = 0.0;

        String specialPkg = Util.captureString(s, "Is this product requires special packaging? (y/n): ");

        if (specialPkg.trim().equalsIgnoreCase("y")) {
            packagePrice = Util.captureDouble(s, "Enter package price: ");
            productPrice += packagePrice;
        }
        if (!DatabaseConnection.saveProductsToDB(statement, productName, sellerName, category, productPrice, packagePrice)) {
            return;
        }
        System.out.println("product added successfully");

    }

    private static void AddProductForBuyer(Scanner s, Connection conn, Statement statement) {  //add product for buyer - option 4
        String buyerName = Util.captureString(s, "Please enter buyer's name\n");

        if (!DatabaseConnection.searchBuyerFromDB(statement, buyerName)) {
            System.out.println("Buyer not found");
            return;
        }

        String sellerName = Util.captureString(s, "Please enter seller's name\n");
        if (!DatabaseConnection.searchSellerFromDB(statement, sellerName)) {
            System.out.println("Seller not found");
            return;
        }
        if (!DatabaseConnection.printSellerItemsFromDB(statement, sellerName)) {
            return;
        }

        String productName = Util.captureString(s, "Please enter product name to purchase\n");

        if (!DatabaseConnection.searchProduct(statement, productName, sellerName)) {
            System.out.println("Product not found");
            return;
        }
        if (DatabaseConnection.checkIfProductIsInSC(statement, buyerName, productName)) {
            System.out.println("Product already in cart");
            return;
        }

        if (!DatabaseConnection.saveProductForBuyerToDB(statement, conn, buyerName, productName, sellerName)) {
            return;
        }
        System.out.println("product added to cart successfully");
    }

    private static void PaymentForBuyer(Scanner s, Statement statement, Connection conn) throws EmptyCartException {  //payment for buyer - option 5
        String buyerName = Util.captureString(s, "Please select the buyer: ");
        if (!DatabaseConnection.searchBuyerFromDB(statement, buyerName)) {
            System.out.println("Buyer not found");
            return;
        }
        if (!DatabaseConnection.checkIfShoppingCartExists(statement, buyerName)) {
            return;
        }
        double totalPrice = DatabaseConnection.getTotalPriceFromDB(conn, buyerName);
        System.out.println("Cart total: " + totalPrice);
        if (!DatabaseConnection.saveOrderHistoryToDB(statement, conn, buyerName)) {
            return;
        }
        System.out.println("Payment successful");
    }

    private static void BuyerInformation(Connection conn) { // buyer information - option 6
        DatabaseConnection.printBuyersFromDB(conn);
    }

    private static void SellerInformation(Connection conn, Statement statement) { // seller information - option 7
        DatabaseConnection.printSellerFromDB(conn, statement);
    }

    private static void displayProductsByCategory(Scanner s, Statement statement) {
        int productCategory = Util.captureIntWithRange(s, List.of("Please select number for product category", "1 :children", "2 :electronics", "3 :office", "4 :clothing"), 1, 4);

        String category;
        if (productCategory == 1) {
            category = "children";
        } else if (productCategory == 2) {
            category = "electronics";
        } else if (productCategory == 3) {
            category = "office";
        } else if (productCategory == 4) {
            category = "clothing";
        } else {
            System.out.println("Invalid category.");
            return;
        }
        DatabaseConnection.printItemsByCategory(statement, category);
    }

    private static void createCartFromHistory(Scanner s, Connection conn, Statement statement) {
        String buyerName = Util.captureString(s, "Please select the buyer: ");

        if (!DatabaseConnection.searchBuyerFromDB(statement, buyerName)) {
            System.out.println("Buyer not found");
            return;
        }
        if (DatabaseConnection.checkIfShoppingCartExists(statement, buyerName)) {
            boolean isDiscard = Util.captureBoolean(s, List.of("Do you want to discard current cart? (y/n) : "), "y", "n");
            if (isDiscard) {
                System.out.println("current cart will be replaced");
                if (!DatabaseConnection.deleteShoppingCartFromDB(statement, conn, buyerName)) {
                    return;
                }
            } else {
                return;
            }
        }

        if (!DatabaseConnection.checkBuyerHistoryFromDB(statement, buyerName)) {
            System.out.println("No order history found for selected buyer");
            return;
        }
        DatabaseConnection.printOrderHistoryFromDB(conn, buyerName);
        int cartID = Util.captureInt(s, "Enter cart ID u want to duplicate");
        if(!DatabaseConnection.checkCartIDHistoryTable(statement, cartID)){
            System.out.println("CartID is not found");
            return;
        }
        if(!DatabaseConnection.restoreCartFromDB(conn, cartID, buyerName)){
            return;
        }
        System.out.println("Shopping cart was created from history");
        }
    }
