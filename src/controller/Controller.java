package controller;

import model.Compte;
import model.Database;
import view.View;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Controller {
    private View view;

    public Controller(View view) {
        this.view = view;
    }

    public void lancerApplication() {
        boolean running = true;
        while (running) {
            int choix = view.menuPrincipal();
            switch (choix) {
                case 1 -> ouvrirCompte();
                case 2 -> deposerArgent();
                case 3 -> retirerArgent();
                case 4 -> transfererArgent();
                case 5 -> consulterSolde();
                case 6 -> listerComptes();
                case 7 -> modifierCompte();
                case 8 -> supprimerCompte();
                case 9 -> running = false;
                default -> System.out.println("Option invalide. Veuillez réessayer.");
            }
        }
    }

    private void ouvrirCompte() {
        String nom = view.demanderNom();
        String prenom = view.demanderPrenom();
        String numeroCompte = generateNumeroCompte();
        double soldeInitial = view.demanderMontant();

        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO Comptes (nom, prenom, numero_compte, solde) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, numeroCompte);
            stmt.setDouble(4, soldeInitial);
            stmt.executeUpdate();
            System.out.println("Compte créé avec succès. Numéro de compte : " + numeroCompte);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création du compte : " + e.getMessage());
        }
    }

    private String generateNumeroCompte() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12).toUpperCase();
    }

    private void deposerArgent() {
        String numeroCompte = view.demanderNumeroCompte();
        double montant = view.demanderMontant();

        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE Comptes SET solde = solde + ? WHERE numero_compte = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, montant);
            stmt.setString(2, numeroCompte);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                String transactionSql = "INSERT INTO Transactions (compte_id, type_transaction, montant) " +
                        "SELECT compte_id, 'Dépôt', ? FROM Comptes WHERE numero_compte = ?";
                PreparedStatement transactionStmt = conn.prepareStatement(transactionSql);
                transactionStmt.setDouble(1, montant);
                transactionStmt.setString(2, numeroCompte);
                transactionStmt.executeUpdate();
                System.out.println("Dépôt réussi.");
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du dépôt : " + e.getMessage());
        }
    }

    private void retirerArgent() {
        String numeroCompte = view.demanderNumeroCompte();
        double montant = view.demanderMontant();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT solde FROM Comptes WHERE numero_compte = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, numeroCompte);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double solde = rs.getDouble("solde");
                if (solde >= montant) {
                    String updateSql = "UPDATE Comptes SET solde = solde - ? WHERE numero_compte = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setDouble(1, montant);
                    updateStmt.setString(2, numeroCompte);
                    updateStmt.executeUpdate();
                    String transactionSql = "INSERT INTO Transactions (compte_id, type_transaction, montant) " +
                            "SELECT compte_id, 'Retrait', ? FROM Comptes WHERE numero_compte = ?";
                    PreparedStatement transactionStmt = conn.prepareStatement(transactionSql);
                    transactionStmt.setDouble(1, montant);
                    transactionStmt.setString(2, numeroCompte);
                    transactionStmt.executeUpdate();
                    System.out.println("Retrait réussi.");
                } else {
                    System.out.println("Solde insuffisant.");
                }
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du retrait : " + e.getMessage());
        }
    }

    private void transfererArgent() {
        System.out.println("Compte débiteur :");
        String numeroCompteSource = view.demanderNumeroCompte();
        System.out.println("Compte créditeur :");
        String numeroCompteDestination = view.demanderNumeroCompte();
        double montant = view.demanderMontant();

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            String soldeQuery = "SELECT solde FROM Comptes WHERE numero_compte = ?";
            PreparedStatement soldeStmt = conn.prepareStatement(soldeQuery);
            soldeStmt.setString(1, numeroCompteSource);
            ResultSet rs = soldeStmt.executeQuery();
            if (rs.next()) {
                double soldeSource = rs.getDouble("solde");
                if (soldeSource >= montant) {
                    String debitSql = "UPDATE Comptes SET solde = solde - ? WHERE numero_compte = ?";
                    PreparedStatement debitStmt = conn.prepareStatement(debitSql);
                    debitStmt.setDouble(1, montant);
                    debitStmt.setString(2, numeroCompteSource);
                    debitStmt.executeUpdate();

                    String creditSql = "UPDATE Comptes SET solde = solde + ? WHERE numero_compte = ?";
                    PreparedStatement creditStmt = conn.prepareStatement(creditSql);
                    creditStmt.setDouble(1, montant);
                    creditStmt.setString(2, numeroCompteDestination);
                    creditStmt.executeUpdate();

                    String transactionSql = "INSERT INTO Transactions (compte_id, type_transaction, montant) " +
                            "SELECT compte_id, 'Transfert', ? FROM Comptes WHERE numero_compte = ?";
                    PreparedStatement transactionStmt = conn.prepareStatement(transactionSql);
                    transactionStmt.setDouble(1, montant);
                    transactionStmt.setString(2, numeroCompteSource);
                    transactionStmt.executeUpdate();

                    conn.commit();
                    System.out.println("Transfert réussi.");
                } else {
                    System.out.println("Solde insuffisant.");
                }
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du transfert : " + e.getMessage());
        }
    }

    private void consulterSolde() {
        String numeroCompte = view.demanderNumeroCompte();

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT solde FROM Comptes WHERE numero_compte = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, numeroCompte);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double solde = rs.getDouble("solde");
                System.out.println("Le solde du compte est : " + solde);
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la consultation du solde : " + e.getMessage());
        }
    }

    private void listerComptes() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM Comptes";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Compte ID : " + rs.getInt("compte_id") + ", Nom : " + rs.getString("nom") +
                        ", Prénom : " + rs.getString("prenom") + ", Numéro : " + rs.getString("numero_compte") +
                        ", Solde : " + rs.getDouble("solde"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la liste des comptes : " + e.getMessage());
        }
    }

    private void modifierCompte() {
        String numeroCompte = view.demanderNumeroCompte();
        String nouveauNom = view.demanderNom();
        String nouveauPrenom = view.demanderPrenom();

        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE Comptes SET nom = ?, prenom = ? WHERE numero_compte = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nouveauNom);
            stmt.setString(2, nouveauPrenom);
            stmt.setString(3, numeroCompte);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Compte modifié avec succès.");
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du compte : " + e.getMessage());
        }
    }

    private void supprimerCompte() {
        String numeroCompte = view.demanderNumeroCompte();

        try (Connection conn = Database.getConnection()) {
            String deleteTransactionSql = "DELETE FROM Transactions WHERE compte_id = (SELECT compte_id FROM Comptes WHERE numero_compte = ?)";
            PreparedStatement deleteTransactionStmt = conn.prepareStatement(deleteTransactionSql);
            deleteTransactionStmt.setString(1, numeroCompte);
            deleteTransactionStmt.executeUpdate();

            String deleteAccountSql = "DELETE FROM Comptes WHERE numero_compte = ?";
            PreparedStatement deleteAccountStmt = conn.prepareStatement(deleteAccountSql);
            deleteAccountStmt.setString(1, numeroCompte);
            int rowsAffected = deleteAccountStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Compte supprimé avec succès.");
            } else {
                System.out.println("Numéro de compte invalide.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du compte : " + e.getMessage());
        }
    }
}