package de.niclashoyer.resteasytest.resource;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
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
            + "language varchar(10),"
            + "version int,"
            + "file varchar(255)"
            + ")";
    protected PreparedStatement pathStatement;
    protected PreparedStatement etagStatement;
    protected PreparedStatement insertStatement;
    protected PreparedStatement singleStatement;
    protected PreparedStatement langUndefStatement;
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
    public Representation createRepresentation(String path, Variant v) {
        MediaType type;
        Locale loc;
        type = v.getMediaType();
        loc = v.getLanguage();
        if (type.isWildcardType() || type.isWildcardSubtype()) {
            return null;
        }
        try {
            java.util.Date nowDate = new java.util.Date();
            Date now = new Date(nowDate.getTime());
            String etag = this.getRandomETag();
            String file = this.getFileName(path, v, 1);
            this.insertStatement.setString(1, path);
            this.insertStatement.setDate(2, now);
            this.insertStatement.setDate(3, now);
            this.insertStatement.setString(4, etag);
            this.insertStatement.setString(5, type.getType());
            this.insertStatement.setString(6, type.getSubtype());
            this.insertStatement.setString(7, loc.toLanguageTag());
            this.insertStatement.setInt(8, 1);
            this.insertStatement.setString(9, file);
            this.insertStatement.executeUpdate();
            File handle = new File(this.path+file);
            return new FileRepresentation(
                    handle,
                    nowDate,
                    nowDate,
                    v,
                    new EntityTag(etag));
        } catch (SQLException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected String getFileName(String path, Variant variant, int version) {
        MediaType type = variant.getMediaType();
        Locale loc = variant.getLanguage();
        return DigestUtils.shaHex(
                path +
                type.getType() + '/' + type.getSubtype() +
                loc.toLanguageTag() +
                version
                ) + ".bin";
    }

    protected String getRandomETag() {
        return new BigInteger(130, random).toString(32).substring(0, 15);
    }

    private void prepareStatements() throws SQLException {
        this.pathStatement = this.conn.prepareStatement(
                "SELECT DISTINCT primarytype, subtype, language "
                + "FROM " + table + " "
                + "WHERE path = ?");
        this.etagStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE etag = ? "
                + "LIMIT 1");
        this.insertStatement = this.conn.prepareStatement(
                "INSERT "
                + "INTO " + table + " VALUES ("
                + "?, ?, ?, ?, ?, ?, ?, ?, ?"
                + ")");
        this.singleStatement = this.conn.prepareStatement(
                "SELECT * "
                + "FROM " + table + " "
                + "WHERE path = ? "
                + "AND primarytype = ? "
                + "AND subtype = ? "
                + "AND language = ? "
                + "LIMIT 1");
        this.langUndefStatement = this.conn.prepareStatement(
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

    @Override
    public List<Variant> getVariants(String path) {
        try {
            boolean hasResults;
            ResultSet rs;
            MediaType mt;
            Locale loc;
            String[] encodings = {"gzip", "deflate"};
            VariantListBuilder list = Variant.encodings(encodings);
            this.pathStatement.setString(1, path);
            rs = this.pathStatement.executeQuery();
            hasResults = false;
            while (rs.next()) {
                hasResults = true;
                mt = new MediaType(rs.getString(1), rs.getString(2));
                loc = new Locale(rs.getString(3));
                list.encodings(encodings);
                list.mediaTypes(mt);
                list.languages(loc);
                list.add();
            }
            if (hasResults) {
                return list.build();
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (SQLException ex) {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Representation selectRepresentation(String path, Variant v) {
        try {
            MediaType type;
            Locale loc;
            File file;
            type = v.getMediaType();
            loc = v.getLanguage();
            if (type == null || type.isWildcardType() || type.isWildcardSubtype()) {
                return null;
            }
            ResultSet rs = this.readRepresentation(path, type, loc);
            System.out.println(rs);
            if (rs == null || !rs.next()) {
                return null;
            } else {
                file = new File(this.path+rs.getString("file"));
                return new FileRepresentation(
                        file,
                        rs.getDate("updated"),
                        rs.getDate("created"),
                        v,
                        new EntityTag(rs.getString("etag")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    protected ResultSet readRepresentation(String path, MediaType type, Locale loc) {
        try {
            String lang = loc.toLanguageTag();
            if (lang.equals("und")) {
                this.langUndefStatement.setString(1, path);
                this.langUndefStatement.setString(2, type.getType());
                this.langUndefStatement.setString(3, type.getSubtype());
                return this.langUndefStatement.executeQuery();
            } else {
                this.singleStatement.setString(1, path);
                this.singleStatement.setString(2, type.getType());
                this.singleStatement.setString(3, type.getSubtype());
                this.singleStatement.setString(4, loc.toLanguageTag());
                return this.singleStatement.executeQuery();
            }
        } catch (SQLException | NullPointerException ex) {
            Logger.getLogger(H2RepresentationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
