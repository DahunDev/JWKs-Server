package com.daniel.Main;


import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Map;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.junit.Test;

import junit.framework.Assert;

public class Database {
	
	/*
	 GPT prompt:
	 how to create table in sqlite in java

something like 
CREATE TABLE IF NOT EXISTS keys(
    kid INTEGER PRIMARY KEY AUTOINCREMENT,
    key BLOB NOT NULL,
    exp INTEGER NOT NULL
)
	 */
    public static void createNewDatabase(String fileName) {

    	  Connection connection = null;
          Statement statement = null;

          try {
              // SQLite database file location
            

              // JDBC URL for SQLite
              String url = "jdbc:sqlite:" + fileName;

              // Establish a connection to the database
              connection = DriverManager.getConnection(url);
              assertNotNull(connection);
              // Create a Statement object
              statement = connection.createStatement();
              assertNotNull(statement);

              // Create the 'keys' table
              String createTableSQL = "CREATE TABLE IF NOT EXISTS keys (" +
                      "kid INTEGER PRIMARY KEY AUTOINCREMENT," +
                      "key BLOB NOT NULL," +
                      "exp INTEGER NOT NULL" +
                      ")";
              
              // Execute the SQL statement to create the table
              statement.execute(createTableSQL);
              
              System.out.println("Table 'keys' created successfully.");

          } catch (SQLException e) {
              e.printStackTrace();
          } finally {
              try {
                  if (statement != null) {
                      statement.close();
                  }
                  if (connection != null) {
                      connection.close();
                  }
              } catch (SQLException e) {
                  e.printStackTrace();
              }
          }
    }
    
    private static Connection connect(String dbFile) {
        // SQLite connection string
        String url = "jdbc:sqlite:" + dbFile;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            assertNotNull(conn);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        assertNotNull(conn);
        return conn;
    }

    public static byte[] PrivateKeyToBLOB( PrivateKey privateKey ){
    	 // Serialize the KeyPair to a byte array
    	PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
    	KeyFactory keyFactory =null;
    	byte[] rs= null;
    	try {
    		keyFactory = KeyFactory.getInstance(privateKey.getAlgorithm());
    		rs = keyFactory.generatePrivate(pkcs8KeySpec).getEncoded();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return rs;
    }

    public static PrivateKey byteToPrivateKey(byte[] data) {
    	PrivateKey key = null;
        try{
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
            key = keyFactory.generatePrivate(keySpec);
        }catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(key);
        return key;
    }
    
    public static PublicKey getPublicKeyFromPrivate( PrivateKey privateKey ) {
    	PublicKey key = null;
    	try {
    		RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
    		RSAPublicKeySpec publickeySpec = new RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), 
    				rsaPrivateCrtKey.getPublicExponent());
    		
    		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    		key = keyFactory.generatePublic(publickeySpec);
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return key;
    }
    
    
    //https://www.sqlitetutorial.net/sqlite-java/select/
    public static void loadKeyPairs(Map<Integer, KeyPairInfo> keyPairs, String dbfile) {
    	String sql = "SELECT kid, key, exp FROM keys";
    	Connection conn = connect(dbfile);
    	
    	try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
            assertNotNull(stmt);
            assertNotNull(rs);

		     // loop through the result set
            while (rs.next()) {
                int kid = rs.getInt("kid");
                byte[] serKey = rs.getBytes("key");
                long exp = rs.getLong("exp") * 1000L; //second to ms
                assertNotNull(serKey);
                PrivateKey privatekey = Database.byteToPrivateKey(serKey);
                PublicKey publicKey = Database.getPublicKeyFromPrivate(privatekey);
                assertNotNull(privatekey);

                JwtsServer.getkeyPairs().put(kid, new KeyPairInfo(publicKey, privatekey, kid, new Date(exp) ));//load data 
                JwtsServer.maxKid = Math.max(kid, JwtsServer.maxKid);
            }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    	
    }
    
    public static void updateKeyPairDB(String dbfile, KeyPairInfo info) {
    	System.out.println("updateKeyPair called");
    	// Assuming you have a 'keypair_table' with a BLOB column named 'keypair_blob' and a unique 'kid' column
    	String insertQuery = "INSERT OR REPLACE INTO keys (kid, key, exp) VALUES (?, ?, ?)";
    	Connection conn = connect(dbfile);
    	try {
            assertNotNull(conn);

    		PreparedStatement statement = conn.prepareStatement(insertQuery);
    		byte[] privatekeyBytes = PrivateKeyToBLOB(info.getPrivateKey());
            assertNotNull(statement);
            assertNotNull(privatekeyBytes);

    	    // Set the values for the 'kid' and 'keypair_blob' columns
    	    statement.setInt(1, info.getKid()); // Replace 1 with your 'kid' value
    	    statement.setBytes(2, privatekeyBytes);
    	    statement.setLong(3, info.getExpiry().getTime() / 1000L); //JWT uses seconds not ms

    	    statement.executeUpdate();
    	    System.out.println("KeyPair size: " + JwtsServer.getkeyPairs().size() );
    	}catch (Exception e){
    		
    	}
    	
    	
    }
    
}