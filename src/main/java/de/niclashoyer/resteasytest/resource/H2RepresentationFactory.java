package de.niclashoyer.resteasytest.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.ext.Provider;
import org.apache.commons.codec.digest.DigestUtils;
import org.h2.jdbcx.JdbcDataSource;

@Provider
public class H2RepresentationFactory implements RepresentationFactory {

    protected SecureRandom random = new SecureRandom();
    protected Connection conn;
    protected final String databaseStructure = "CREATE TABLE IF NOT EXISTS "
            + "representations ("
            + "path text,"
            + "created datetime,"
            + "updated datetime,"
            + "etag varchar(128),"
            + "primarytype varchar(20),"
            + "subtype varchar(20),"
            + "version int,"
            + "file varchar(255)"
            + ")";
    protected PreparedStatement pathStatement;
    protected PreparedStatement etagStatement;
    protected PreparedStatement primarytypeStatement;
    protected PreparedStatement basetypeStatement;
    protected PreparedStatement insertStatement;
    protected PreparedStatement singleStatement;
    protected PreparedStatement ETagUpdateStatement;
    protected PreparedStatement anyStatement;
    private final String path = "representations/";
    private final String table = "representations";

    public H2RepresentationFactory() throws SQLException {
        JdbcDataSource ds;
        Statement stmt;
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:" + this.path + "representations");
        ds.setUser("sa");
        ds.setPassword("sa");
        this.conn = ds.getConnection();
        stmt = this.conn.createStatement();
        stmt.executeUpdate(databaseStructure);
        stmt.close();
        this.prepareStatements();
    }

    @Override
    public Collection<MimeType> getTypes(String path) {
        try {
            ResultSet rs;
            MimeType mt;
            ArrayList<MimeType> list = new ArrayList<>();
            this.pathStatement.setString(1, path);
            rs = this.pathStatement.executeQuery();
            while (rs.next()) {
                try {
                    mt = new MimeType(rs.getString(1), rs.getString(2));
                    list.add(mt);
                } catch (MimeTypeParseException ex) {
                }
            }
            return list;
        } catch (SQLException ex) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Representation readRepresentation(String path, MimeType type) {
        try {
            ResultSet rs;
            if ("*".equals(type.getPrimaryType())) {
                this.anyStatement.setString(1, path);
                rs = this.anyStatement.executeQuery();
                return this.toRepresentation(rs);
            }
            if ("*".equals(type.getSubType())) {
                this.primarytypeStatement.setString(1, path);
                this.primarytypeStatement.setString(2, type.getPrimaryType());
                rs = this.primarytypeStatement.executeQuery();
                return this.toRepresentation(rs);
            }
            this.singleStatement.setString(1, path);
            this.singleStatement.setString(2, type.getPrimaryType());
            this.singleStatement.setString(3, type.getSubType());
            rs = this.singleStatement.executeQuery();
            return this.toRepresentation(rs);
        } catch (SQLException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Representation writeRepresentation(String path, MimeType type) {
        Representation rep;
        if ("*".equals(type.getPrimaryType()) || "*".equals(type.getSubType())) {
            return null;
        }
        rep = this.readRepresentation(path, type);
        if (rep == null) {
            rep = this.addNewRepresentation(path, type);
        } else {
            try {
                String oldETag = rep.getETag();
                String newETag = this.getRandomETag();
                this.ETagUpdateStatement.setString(1, newETag);
                this.ETagUpdateStatement.setString(2, oldETag);
                this.ETagUpdateStatement.executeUpdate();
                rep.setETag(newETag);
                return rep;
            } catch (SQLException ex) {
                Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return rep;
    }

    protected Representation addNewRepresentation(String path, MimeType type) {
        if ("*".equals(type.getPrimaryType()) || "*".equals(type.getSubType())) {
            return null;
        }
        try {
            java.util.Date nowDate = new java.util.Date();
            Date now = new Date(nowDate.getTime());
            String etag = this.getRandomETag();
            String file = this.getFileName(path, type);
            this.insertStatement.setString(1, path);
            this.insertStatement.setDate(2, now);
            this.insertStatement.setDate(3, now);
            this.insertStatement.setString(4, etag);
            this.insertStatement.setString(5, type.getPrimaryType());
            this.insertStatement.setString(6, type.getSubType());
            this.insertStatement.setInt(7, 1);
            this.insertStatement.setString(8, file);
            this.insertStatement.executeUpdate();
            Representation rep = new Representation();
            rep.setFileForStreams(new File(this.path + file));
            rep.setPath(path);
            rep.setETag(etag);
            rep.setMimetype(type);
            rep.setCreated(nowDate);
            rep.setUpdated(nowDate);
            rep.setVersion(1);
            return rep;
        } catch (FileNotFoundException | SQLException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected String getFileName(String path, MimeType type) {
        return DigestUtils.shaHex(path + type.getBaseType()) + ".bin";
    }

    protected String getRandomETag() {
        return new BigInteger(130, random).toString(32).substring(0, 15);
    }

    protected Representation toRepresentation(ResultSet rs) {
        try {
            if (rs.next()) {
                Representation rep = new Representation();
                String name = rs.getString("file");
                File file = new File(this.path + name);
                rep.setFileForStreams(file);
                rep.setPath(path);
                rep.setMimetype(new MimeType(rs.getString("primarytype"), rs.getString("subtype")));
                rep.setETag(rs.getString("etag"));
                rep.setVersion(rs.getInt("version"));
                rep.setCreated(rs.getDate("created"));
                rep.setUpdated(rs.getDate("updated"));
                return rep;
            } else {
                return null;
            }
        } catch (SQLException | MimeTypeParseException | FileNotFoundException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void prepareStatements() throws SQLException {
        this.pathStatement = this.conn.prepareStatement(
                "SELECT DISTINCT primarytype, subtype "
                + "FROM " + table + " "
                + "WHERE path = ?");
        this.etagStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE etag = ? "
                + "LIMIT 1");
        this.primarytypeStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE path = ? "
                + "AND primarytype = ? "
                + "LIMIT 1");
        this.basetypeStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE path = ? "
                + "AND primarytype = ? "
                + "AND subtype = ?");
        this.insertStatement = this.conn.prepareStatement(
                "INSERT "
                + "INTO " + table + " VALUES ("
                + "?, ?, ?, ?, ?, ?, ?, ?"
                + ")");
        this.singleStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE path = ? "
                + "AND primarytype = ? "
                + "AND subtype = ? "
                + "LIMIT 1");
        this.anyStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE path = ? "
                + "LIMIT 1");
        this.ETagUpdateStatement = this.conn.prepareStatement(
                "UPDATE " + table + " "
                + "SET etag = ? "
                + "WHERE etag = ?");
    }
}
