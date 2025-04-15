import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;

public class family_finance_management {
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
        family_finance_management ffm = new family_finance_management();
        try {
            int i = 1;
            while (i > 0) {
                System.out.println("Was möchtest du tun?");
                System.out.println("(0) Programm beenden (1) Daten Ausgeben (2) Nutzer hinzufügen (3) Nutzer Ausgeben (4) Datenbanken Zurücksetzen (5) Eintrag hinzufügen");
                switch (ffm.sc.nextInt()) {
                    case 0:
                        System.exit(0);
                        break;
                    case 1:
                        ffm.einträgeAusgeben(ffm.connection);
                        break;
                    case 2:
                        ffm.nutzerhinzufuegen(ffm.connection);
                        break;
                    case 3:
                        ffm.nutzerausgeben(ffm.connection);
                        break;
                    case 4:
                        ffm.resetDatabase(ffm.connection);
                        break;
                    case 5:
                        ffm.eintragHinzufügen(ffm.connection);
                }
            }
            ffm.einträgeAusgeben(ffm.connection);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void einträgeAusgeben(Connection connection) throws SQLException {
        int nid1 = 0;
        int nid2 = 0;
        System.out.println("Zwischen welchen 2 Nutzern soll der Schuldenstand ermittelt werden?");
        nutzerausgeben(connection);
        System.out.print("Nutzer 1: ");
        nid1 = sc.nextInt();
        System.out.println("");
        System.out.print("Nutzer 2: ");
        nid2 = sc.nextInt();
        Statement allesAusgebenStatement = connection.createStatement();
        ResultSet resultSet = allesAusgebenStatement.executeQuery("Select IDeintrag, datum, betrag, stand , kommentar, nutzername from nutzer, einträge where (vorstreckerID OR bezahlerID) = " + nid1 + ") AND (vorstreckerID OR bezahlerID) = " + nid2 + ") order by datum;\n");

        while (resultSet.next()) {
            System.out.print(resultSet.getDate("datum"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("betrag"));
            System.out.print("  ");
            System.out.print(resultSet.getInt("stand"));
            System.out.print("  ");
            System.out.print(resultSet.getString("kommentar"));
            System.out.print("  ");

        }
        allesAusgebenStatement.close();
        resultSet.close();
    }

    public void nutzerausgeben(Connection connection) throws SQLException {
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

    public void nutzerhinzufuegen(Connection connection) throws SQLException {
        System.out.print("Geben sie einen Nutzernamen an: ");
        String nutzername = sc.next();
        System.out.print("Geben sie ein Passwort an: ");
        String passwort = sc.next();
        PreparedStatement stnutzerHinzufuegen = connection.prepareStatement("insert into nutzer values\n" +
                "(null, '" + nutzername + "', '" + passwort + "');");
        stnutzerHinzufuegen.execute();
        stnutzerHinzufuegen.close();
    }

    public void resetDatabase(Connection connection) throws SQLException {
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

    public void eintragHinzufügen(Connection connection) throws SQLException {
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
        nutzerausgeben(connection);
        System.out.println("Vorstreckender: (ID)");
        vorstrID = sc.nextInt();
        System.out.println("Bezahlender: (ID)");
        bezID = sc.nextInt();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("Select betrag from einträge");
        while (rs.next()) {
            stand += rs.getDouble("betrag");
        }
        PreparedStatement ps = connection.prepareStatement("insert into einträge values (null, '" + datum + "' , " + betrag + ", " + stand + ", '" + kommentar + "', " + vorstrID + ", " + bezID+ ");");
        ps.execute();
    }
}
