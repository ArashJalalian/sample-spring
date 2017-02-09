CREATE TABLE IF NOT EXISTS `test`.`customer_transactions` (
    `account_number` BIGINT NOT NULL,
    `amount` DECIMAL(13,2) NOT NULL)
    ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `test`.`customer_balance` (
    `account_number` INT NOT NULL,
    `balance` DECIMAL(13,2) NOT NULL,
    UNIQUE INDEX `account_number_idx` (`account_number` ASC))
    ENGINE = InnoDB;

DELIMITER $$
CREATE TRIGGER `test`.`customer_transactions_AFTER_INSERT` AFTER INSERT ON `customer_transactions` FOR EACH ROW
    BEGIN
        IF NOT EXISTS(SELECT * FROM customer_balance WHERE account_number=new.account_number) THEN
            INSERT INTO customer_balance (account_number, balance) VALUES (new.account_number, ((-1) * new.amount));
		ELSE 
			UPDATE customer_balance SET balance=balance-new.amount 
            WHERE account_number=new.account_number;
        END IF;
    END $$
DELIMITER ;