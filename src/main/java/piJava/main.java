package piJava;

import piJava.entities.user;
import piJava.services.ClasseService;
import piJava.services.UserServices;
import piJava.services.loginService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        UserServices userService = new UserServices();
        loginService loginService = new loginService();
        Scanner sc = new Scanner(System.in);

        int choice;
        ClasseService classeService = new ClasseService();
        System.out.println(classeService.getAllClasses());

        do {
            System.out.println("\n===== USER MANAGEMENT MENU =====");
            System.out.println("1. Register (Add User)");
            System.out.println("2. Display All Users");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. Login");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {

                case 1: // Add user
                    try {
                        user newUser = new user();

                        System.out.print("Email: ");
                        newUser.setEmail(sc.nextLine());

                        System.out.print("Password: ");
                        newUser.setPassword(sc.nextLine());

                        System.out.print("First Name: ");
                        newUser.setPrenom(sc.nextLine());

                        System.out.print("Last Name: ");
                        newUser.setNom(sc.nextLine());

                        System.out.print("Phone: ");
                        newUser.setNum_tel(sc.nextLine());

                        System.out.print("Date of Birth (yyyy-MM-dd): ");
                        newUser.setDate_de_naissance(sc.nextLine());

                        System.out.print("Gender (Homme/Femme): ");
                        newUser.setSexe(sc.nextLine());

                        userService.add(newUser);

                    } catch (Exception e) {
                        System.err.println("Error adding user: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 2: // Display all
                    List<user> users = userService.show();
                    for (user u : users) {
                        System.out.println(u);
                    }
                    break;

                case 3: // Update
                    try {
                        user updateUser = new user();

                        System.out.print("ID to update: ");
                        updateUser.setId(sc.nextInt());
                        sc.nextLine();

                        System.out.print("New Email: ");
                        updateUser.setEmail(sc.nextLine());

                        System.out.print("New First Name: ");
                        updateUser.setPrenom(sc.nextLine());

                        System.out.print("New Last Name: ");
                        updateUser.setNom(sc.nextLine());

                        System.out.print("New Phone: ");
                        updateUser.setNum_tel(sc.nextLine());

                        System.out.print("New Date of Birth (yyyy-MM-dd): ");
                        updateUser.setDate_de_naissance(sc.nextLine());

                        System.out.print("New Gender (Homme/Femme): ");
                        updateUser.setSexe(sc.nextLine());

                        userService.edit(updateUser);

                    } catch (Exception e) {
                        System.err.println("Error updating user: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 4: // Delete
                    try {
                        System.out.print("ID to delete: ");
                        int idToDelete = sc.nextInt();
                        sc.nextLine();
                        userService.delete(idToDelete);
                    } catch (Exception e) {
                        System.err.println("Error deleting user: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 5: // Login
                    try {
                        System.out.print("Email: ");
                        String email = sc.nextLine();
                        System.out.print("Password: ");
                        String password = sc.nextLine();

                        user loggedUser = loginService.login(email, password);
                        if (loggedUser != null) {
                            System.out.println("✅ Logged in successfully!");
                            System.out.println("User: " + loggedUser.getPrenom() + " " + loggedUser.getNom() +
                                    " | Email: " + loggedUser.getEmail() +
                                    " | Roles: " + loggedUser.getRoles());
                        } else {
                            System.out.println("❌ Login failed!");
                        }

                    } catch (Exception e) {
                        System.err.println("Error logging in: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 0:
                    System.out.println("Exiting...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 0);

        sc.close();
    }
}