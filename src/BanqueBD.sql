CREATE DATABASE BanqueDB;

USE BanqueDB;

CREATE TABLE Comptes (
                         compte_id INT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(100) NOT NULL,
                         prenom VARCHAR(100) NOT NULL,
                         numero_compte VARCHAR(20) UNIQUE NOT NULL,
                         solde DECIMAL(15, 2) DEFAULT 0.0
);

CREATE TABLE Transactions (
                              transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                              compte_id INT,
                              type_transaction VARCHAR(50) NOT NULL,
                              montant DECIMAL(15, 2) NOT NULL,
                              date_transaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (compte_id) REFERENCES Comptes(compte_id)
);