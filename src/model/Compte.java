package model;

public class Compte {
    private int id;
    private String nom;
    private String prenom;
    private String numeroCompte;
    private double solde;

    public Compte(int id, String nom, String prenom, String numeroCompte, double solde) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.numeroCompte = numeroCompte;
        this.solde = solde;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getNumeroCompte() { return numeroCompte; }
    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }
}
