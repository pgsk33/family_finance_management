import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class FamilyFinanceManagement {
    Connection connection;

    {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://root@localhost:3306/schuldenapp");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        FamilyFinanceManagement ffm = new FamilyFinanceManagement();
        ffm.mainLoop();
    }

    private void mainLoop() {
        try {
            while (true) {
                System.out.println("Was möchtest du tun?");
                System.out.println("(0) Programm beenden (1) Daten Ausgeben (2) Nutzer hinzufügen (3) Nutzer Ausgeben (4) Datenbanken Zurücksetzen (5) Eintrag hinzufügen");
                switch (sc.nextInt()) {
                    case 0:
                        System.exit(0);
                    case 1:
                        einträgeAusgeben();
                        break;
                    case 2:
                        nutzerhinzufuegen();
                        break;
                    case 3:
                        nutzerausgeben();
                        break;
                    case 4:
                        resetDatabase();
                        break;
                    case 5:
                        eintragHinzufügen();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void einträgeAusgeben() throws SQLException {
        double stand = 0;
        System.out.println("Zwischen welchen 2 Nutzern soll der Schuldenstand ermittelt werden?");
        int nid1 = getNid("1");
        int nid2 = getNid("1");

        Statement allesAusgebenStatement = connection.createStatement();
        ResultSet resultSet = allesAusgebenStatement.executeQuery("SELECT e.IDeintrag, e.datum, e.betrag, e.stand, e.kommentar, n1.nutzername AS vorstrecker_name, n2.nutzername AS bezahler_name, e.vorstreckerID, e.bezahlerID " +
                "FROM einträge e " +
                "LEFT JOIN nutzer n1 ON e.vorstreckerID = n1.IDnutzer " +
                "LEFT JOIN nutzer n2 ON e.bezahlerID = n2.IDnutzer " +
                "WHERE (e.vorstreckerID = " + nid1 + " AND e.bezahlerID = " + nid2 + ") OR (e.vorstreckerID = " + nid2 + " AND e.bezahlerID = " + nid1 + ") OR e.vorstreckerID = " + nid1 + " OR e.bezahlerID = " + nid1 + " OR e.vorstreckerID = " + nid2 + " OR e.bezahlerID = " + nid2 + " " +
                "ORDER BY e.datum;");
        while (resultSet.next()) {
            System.out.print(resultSet.getDate("datum"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("betrag"));
            System.out.print("  ");

            if (nid1 == resultSet.getInt("vorstreckerID")){
                stand += resultSet.getInt("betrag");
            } else {
                stand -= resultSet.getInt("betrag");
            }

            System.out.print(stand + "   ");
            System.out.print(resultSet.getString("kommentar"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("vorstreckerID"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("bezahlerID"));
            System.out.println("  ");
        }
        allesAusgebenStatement.close();
        resultSet.close();
        Statement myps = connection.createStatement();
        int searchID = nid1;
        String nid1nn = "error";
        String nid2nn = "error";
        ResultSet myrs = myps.executeQuery("Select nutzername from nutzer where IDnutzer = " + searchID + ";");
        while (myrs.next()){
            nid1nn = myrs.getString("nutzername");
        }
        searchID = nid2;
        ResultSet myrs2 = myps.executeQuery("Select nutzername from nutzer where IDnutzer = " + searchID + ";");
        while (myrs2.next()){
            nid2nn = myrs2.getString("nutzername");
        }
        System.out.println(nid1nn + " bekommt von " + nid2nn + " " + stand + " Euro.");
    }

    private int getNid(String userID) throws SQLException {
        nutzerausgeben();
        System.out.print("Nutzer id " + userID);
        return sc.nextInt();
    }

    public void nutzerausgeben() throws SQLException {
        Statement allesAusgebenStatement = connection.createStatement();
        ResultSet resultSet = allesAusgebenStatement.executeQuery("Select IDnutzer, nutzername from nutzer");
        while (resultSet.next()) {
            System.out.print(resultSet.getInt("IDnutzer"));
            System.out.print("  ");
            System.out.print(resultSet.getString("nutzername"));
            System.out.println("");
        }
        resultSet.close();
        allesAusgebenStatement.close();
    }

    public void nutzerhinzufuegen() throws SQLException {
        System.out.print("Geben sie einen Nutzernamen an: ");
        String nutzername = sc.next();
        System.out.print("Geben sie ein Passwort an: ");
        String passwort = sc.next();
        PreparedStatement stnutzerHinzufuegen = connection.prepareStatement("insert into nutzer values\n" +
                "(null, '" + nutzername + "', '" + passwort + "');");
        stnutzerHinzufuegen.execute();
        stnutzerHinzufuegen.close();
    }

    public void resetDatabase() throws SQLException {
        Statement rdata = connection.createStatement();
        rdata.executeUpdate("drop database if exists schuldenapp;");
        rdata.executeUpdate("create database schuldenapp;");
        rdata.executeUpdate("use schuldenapp;");
        rdata.executeUpdate("create table nutzer(\n" +
                "IDnutzer int Not Null auto_increment,\n" +
                "nutzername varchar(10) not null default '',\n" +
                "passwort varchar(20) not null default '',\n" +
                "primary key (IDnutzer));");
        rdata.executeUpdate("create table einträge(\n" +
                "IDeintrag int(2) NOT NULL auto_increment,\n" +
                "datum datetime,\n" +
                "betrag decimal NOT NULL,\n" +
                "stand decimal,\n" +
                "kommentar varchar(255) NOT NULL default '',\n" +
                "vorstreckerID int(2),\n" +
                "bezahlerID int(2),\n" +
                "primary key (IDeintrag),\n" +
                "foreign key (vorstreckerID) REFERENCES nutzer(IDnutzer),\n" +
                "foreign key (bezahlerID) REFERENCES nutzer(IDnutzer)\n" +
                ");");
        rdata.close();
    }

    public void eintragHinzufügen() throws SQLException {
        String datum = "1111-11-11";
        double betrag = 0;
        double stand = 0;
        String kommentar = "Keine Nutzereingabe";
        int vorstrID = 0;
        int bezID = 0;

        System.out.println("Welche Datumsoption? (1) Heute  (2) Eigenes Datum?");
        switch (sc.nextInt()) {
            case 1:
                datum = String.valueOf(LocalDate.now());
                break;
            case 2:
                System.out.println("Datum eingeben (YYYY-MM-DD)");
                datum = sc.next();
                break;
        }
        System.out.println("Betrag:");
        betrag = sc.nextDouble();
        System.out.println("Kommentar (MAX 255 CHARACTERS)");
        kommentar = sc.next();
        nutzerausgeben();
        System.out.println("Vorstreckender: (ID)");
        vorstrID = sc.nextInt();
        System.out.println("Bezahlender: (ID)");
        bezID = sc.nextInt();
        PreparedStatement ps = connection.prepareStatement("insert into einträge values (null, '" + datum + "' , " + betrag + ", " + stand + ", '" + kommentar + "', " + vorstrID + ", " + bezID+ ");");
        ps.execute();
    }
}

