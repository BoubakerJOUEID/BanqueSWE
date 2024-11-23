
package view;

import java.util.Scanner;

public class View {
    private Scanner scanner = new Scanner(System.in);

    public int menuPrincipal() {
        System.out.println("\nMenu Principal:");
        System.out.println("1. Ouvrir un compte");
        System.out.println("2. Déposer de l'argent");
        System.out.println("3. Retirer de l'argent");
        System.out.println("4. Transférer de l'argent");
        System.out.println("5. Consulter un solde");
        System.out.println("6. Voir la liste des comptes");
        System.out.println("7. Modifier un compte");
        System.out.println("8. Supprimer un compte");
        System.out.println("9. Quitter");
        System.out.print("Choisissez une option : ");
        return scanner.nextInt();
    }

    public String demanderNom() {
        System.out.print("Nom : ");
        return scanner.next();
    }

    public String demanderPrenom() {
        System.out.print("Prénom : ");
        return scanner.next();
    }

    public String demanderNumeroCompte() {
        System.out.print("Numéro de compte : ");
        return scanner.next();
    }

    public double demanderMontant() {
        System.out.print("Montant : ");
        return scanner.nextDouble();
    }
}
