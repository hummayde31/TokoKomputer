/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tokokomputer.Pengaturan;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Humaidi
 */
public class Koneksi {
    private final String url;
    
    public Koneksi() {
        this.url = "jdbc:mysql://localhost:3306/db_penjualan?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    }
    
    
    public Connection getKoneksi(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url,"root","");
        } catch (SQLException ex) {
            Logger.getLogger(Koneksi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
}
