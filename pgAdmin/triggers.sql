CREATE OR REPLACE FUNCTION prevent_duplicate_user_type()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'sellertable' THEN
        IF EXISTS (SELECT 1 FROM buyerTable WHERE buyerUsername = NEW.sellerUsername) THEN
            RAISE EXCEPTION 'User "%" already exists as a buyer.', NEW.sellerUsername;
        END IF;
    END IF;

    IF TG_TABLE_NAME = 'buyertable' THEN
        IF EXISTS (SELECT 1 FROM sellerTable WHERE sellerUsername = NEW.buyerUsername) THEN
            RAISE EXCEPTION 'User "%" already exists as a seller.', NEW.buyerUsername;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER check_duplicate_in_seller
BEFORE INSERT ON sellerTable
FOR EACH ROW
EXECUTE FUNCTION prevent_duplicate_user_type();

CREATE TRIGGER check_duplicate_in_buyer
BEFORE INSERT ON buyerTable
FOR EACH ROW
EXECUTE FUNCTION prevent_duplicate_user_type();