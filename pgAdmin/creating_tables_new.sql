-- creating tables

CREATE TABLE details(
   userName VARCHAR(50) PRIMARY KEY,
   pwd VARCHAR(50) NOT NULL
);

CREATE TABLE buyerTable(
 buyerUserName VARCHAR(50) PRIMARY KEY,
 country VARCHAR(50) NOT NULL,
 city VARCHAR(50) NOT NULL,
 buildingNumber INT NOT NULL,
 streetName VARCHAR(50) NOT NULL,
 FOREIGN KEY (buyerUserName) REFERENCES details(userName)
);

CREATE TABLE sellerTable(
   sellerUserName VARCHAR(50) PRIMARY KEY,
   FOREIGN KEY (sellerUserName) REFERENCES details(userName)
);

CREATE TABLE productTable(
  productName VARCHAR(50) NOT NULL,
  sellerUserName VARCHAR(50) NOT NULL,
  category VARCHAR(50) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (productName, sellerUserName),
  FOREIGN KEY (sellerUserName) REFERENCES sellerTable(sellerUserName)
);

CREATE TABLE specialPackaging(
 productName VARCHAR(50) NOT NULL,
 sellerUserName VARCHAR(50) NOT NULL,
 extraPrice DECIMAL(10,2),
 PRIMARY KEY (productName, sellerUserName),
 FOREIGN KEY (productName, sellerUserName) REFERENCES productTable(productName, sellerUserName)
);

CREATE TABLE shoppingCartTable(
cartID SERIAL PRIMARY KEY,
totalPrice DECIMAL(10,2) NOT NULL,
buyerUserName VARCHAR(50) NOT NULL,
FOREIGN KEY (buyerUserName) REFERENCES buyerTable(buyerUserName)
);


CREATE TABLE cartProductTable(
 cartID SERIAL NOT NULL,
 productName VARCHAR(50) NOT NULL,
 sellerUserName VARCHAR(50) NOT NULL,
 PRIMARY KEY(cartID, productName, sellerUserName),
 FOREIGN KEY (productName, sellerUserName) REFERENCES productTable(productName, sellerUserName)
);

CREATE TABLE orderHistoryTable( 
 orderTimestamp VARCHAR(50) NOT NULL,
 buyerUserName VARCHAR(50) NOT NULL,
 cartID SERIAL NOT NULL,
 PRIMARY KEY(buyerUserName,cartID),
 FOREIGN KEY (buyerUserName) REFERENCES buyertable(buyerUserName)
);


