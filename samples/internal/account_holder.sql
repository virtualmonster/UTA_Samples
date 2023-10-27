-- H2 2.2.222;
CREATE TABLE "ACCOUNT"(
    "ID" NUMERIC(5) NOT NULL,
    "NAME" CHARACTER VARYING(12) NOT NULL
);
ALTER TABLE "ACCOUNT" ADD CONSTRAINT "ACCOUNT_PK" PRIMARY KEY("ID");
CREATE TABLE "HOLDER"(
    "ID" NUMERIC(5) NOT NULL,
    "ACCOUNT_ID" NUMERIC(5) NOT NULL,
    "NAME" CHARACTER VARYING(12) NOT NULL
);
ALTER TABLE "HOLDER" ADD CONSTRAINT "HOLDER_PK" PRIMARY KEY("ID");
ALTER TABLE "HOLDER" ADD CONSTRAINT "HOLDER_ACCOUNT_ID_FK" FOREIGN KEY("ACCOUNT_ID") REFERENCES "ACCOUNT"("ID") ON DELETE CASCADE NOCHECK;
CREATE TABLE "TRANSFER"(
    "ID" NUMERIC(5) NOT NULL,
    "ACCOUNT_ID" NUMERIC(5) NOT NULL,
    "AMOUNT" NUMERIC(5) NOT NULL,
    "NAME" CHARACTER VARYING(12) NOT NULL
);
ALTER TABLE "TRANSFER" ADD CONSTRAINT "TRANSFER_PK" PRIMARY KEY("ID");
ALTER TABLE "TRANSFER" ADD CONSTRAINT "TRANSFER_ACCOUNT_ID_FK" FOREIGN KEY("ACCOUNT_ID") REFERENCES "ACCOUNT"("ID") ON DELETE CASCADE NOCHECK;  

INSERT INTO "ACCOUNT" VALUES
(1, 'Current'),
(2, 'Joint'),
(3, 'Credit');
INSERT INTO "HOLDER" VALUES
(1, 1, 'Alice'),
(2, 2, 'Bob'),
(3, 2, 'Claire'),
(4, 3, 'Derek');   
INSERT INTO "TRANSFER" VALUES
(1, 1, 100, 'T1'),
(2, 2, 200, 'T2'),
(3, 2, 300, 'T3'),
(4, 2, 400, 'T4'),
(5, 3, 500, 'T5'),
(6, 3, 600, 'T6');

-- 3 views
SELECT id AS account_id, name AS account_name
FROM account;
SELECT account_id AS holder_account_id, name AS holder_name
FROM holder;
SELECT account_id AS transaction_account_id, amount AS transaction_amount, name AS transaction_name
FROM transfer;
-- 2 views (join)
SELECT a.id AS account_id, a.name AS account_name, h.name AS holder_name
FROM holder h
JOIN account a ON a.id = h.account_id;
SELECT account_id AS transaction_account_id, amount AS transaction_amount, name AS transaction_name
FROM transfer;
-- 1 view (union)
SELECT 'account' AS tab, id AS account_id, name AS account_name, null AS holder_name, null AS transaction_amount, null AS transaction_name
FROM account
UNION
SELECT 'holder', account_id, null, name, null, null
FROM holder
UNION
SELECT 'transaction', account_id, null, null, amount, name 
FROM transfer