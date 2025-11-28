-- inserting data to data base

INSERT INTO details (userName, pwd) VALUES
('daniel.katz', 'dkPass123'),
('maria.levy', 'mlPass456'),
('yossi.goldberg', 'ygPass789');

INSERT INTO buyerTable (buyerUserName, country, city, streetName, buildingNumber) VALUES
('maria.levy', 'Israel', 'Haifa', 'Hertzel Street', 14),
('yossi.goldberg', 'USA', 'San Francisco', 'Mission Street', 22);

INSERT INTO sellerTable (sellerUserName) VALUES
('daniel.katz');

INSERT INTO productTable (productName, sellerUserName, category, price) VALUES
('Wireless Mouse', 'daniel.katz', 'electronics', 79.90),
('Reusable Bottle', 'daniel.katz', 'office', 49.00),
('Toddler Puzzle Set', 'daniel.katz', 'children', 39.90),
('Cotton T-Shirt', 'daniel.katz', 'clothing', 59.90);

INSERT INTO specialPackaging (productName, sellerUserName, extraPrice) VALUES
('Wireless Mouse', 'daniel.katz', 9.99),
('Reusable Bottle', 'daniel.katz', 4.50),
('Toddler Puzzle Set', 'daniel.katz', 6.00),
('Cotton T-Shirt', 'daniel.katz', 3.00);

INSERT INTO shoppingCartTable (cartID, buyerUserName, totalPrice) VALUES
(1, 'maria.levy', 189.30),
(2, 'yossi.goldberg', 49.00);

INSERT INTO cartProductTable (cartID, productName, sellerUserName) VALUES
(1, 'Wireless Mouse', 'daniel.katz'),
(2, 'Reusable Bottle', 'daniel.katz'),
(3, 'Reusable Bottle', 'daniel.katz');

