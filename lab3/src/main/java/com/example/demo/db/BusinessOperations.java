
package com.example.demo.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class BusinessOperations {
    private BusinessOperations() {}

    private static Connection connection() throws SQLException {
        if (Database.connection == null || Database.connection.isClosed()) Database.connect();
        return Database.connection;
    }

    public static String publishItem(long userId, String about, String condition,
                                     long startingPrice, String step, String endDate) {
        Connection c = null;
        try {
            c = connection();
            c.setAutoCommit(false);
            try (PreparedStatement owner = c.prepareStatement("SELECT id FROM users WHERE id=?")) {
                owner.setLong(1, userId);
                try (ResultSet rs = owner.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "User not found"; }
                }
            }
            long itemId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO items(user_id,about,cndn) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId); ps.setString(2, about); ps.setString(3, condition); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("No item id generated"); itemId = keys.getLong(1); }
            }
            long lotId;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO lots(item_id,pprice,start_price,step,date,status) VALUES(?,?,?,?,?, 'OPEN')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, itemId); ps.setLong(2, startingPrice); ps.setLong(3, startingPrice); ps.setString(4, step); ps.setString(5, endDate); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("No lot id generated"); lotId = keys.getLong(1); }
            }
            c.commit();
            return "Published itemId=" + itemId + ", lotId=" + lotId;
        } catch (SQLException e) { rollback(c); return "Publish failed: " + e.getMessage(); }
        finally { resetAutoCommit(c); }
    }

    public static String placeBid(long lotId, long userId, long price, String date) {
        Connection c = null;
        try {
            c = connection(); c.setAutoCommit(false);
            long currentPrice;
            try (PreparedStatement ps = c.prepareStatement("SELECT pprice,start_price,status FROM lots WHERE id=? FOR UPDATE")) {
                ps.setLong(1, lotId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "Lot not found"; }
                    if (!"OPEN".equalsIgnoreCase(rs.getString("status"))) { c.rollback(); return "Lot is not open"; }
                    currentPrice = rs.getLong("pprice");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM users WHERE id=?")) {
                ps.setLong(1, userId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "User not found"; }
                }
            }
            if (price <= currentPrice) { c.rollback(); return "Bid must exceed current price " + currentPrice; }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO birds(lot_id,user_id,price,date) VALUES(?,?,?,?)")) {
                ps.setLong(1, lotId); ps.setLong(2, userId); ps.setLong(3, price); ps.setString(4, date); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE lots SET pprice=? WHERE id=?")) {
                ps.setLong(1, price); ps.setLong(2, lotId); ps.executeUpdate();
            }
            c.commit(); return "Bid accepted; currentPrice=" + price;
        } catch (SQLException e) { rollback(c); return "Bid failed: " + e.getMessage(); }
        finally { resetAutoCommit(c); }
    }

    public static String closeAuction(long lotId) {
        Connection c = null;
        try {
            c = connection(); c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("SELECT status FROM lots WHERE id=? FOR UPDATE")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "Lot not found"; }
                    if (!"OPEN".equalsIgnoreCase(rs.getString("status"))) { c.rollback(); return "Lot is not open"; }
                }
            }
            long bidId, winnerId, price;
            try (PreparedStatement ps = c.prepareStatement("SELECT id,user_id,price FROM birds WHERE lot_id=? ORDER BY price DESC, id ASC LIMIT 1")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "Cannot close an auction without bids"; }
                    bidId = rs.getLong("id"); winnerId = rs.getLong("user_id"); price = rs.getLong("price");
                }
            }
            long dealId;
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO deals(lot_id,bird_id,price,status) VALUES(?,?,?,'COMPLETED')", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, lotId); ps.setLong(2, bidId); ps.setLong(3, price); ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("No deal id generated"); dealId = keys.getLong(1); }
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE lots SET status='CLOSED',pprice=? WHERE id=?")) {
                ps.setLong(1, price); ps.setLong(2, lotId); ps.executeUpdate();
            }
            c.commit(); return "Auction closed; dealId=" + dealId + ", winnerUserId=" + winnerId + ", price=" + price;
        } catch (SQLException e) { rollback(c); return "Close failed: " + e.getMessage(); }
        finally { resetAutoCommit(c); }
    }

    public static String cancelAuction(long lotId) {
        Connection c = null;
        try {
            c = connection(); c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("SELECT status FROM lots WHERE id=? FOR UPDATE")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "Lot not found"; }
                    if (!"OPEN".equalsIgnoreCase(rs.getString("status"))) { c.rollback(); return "Only open lots can be cancelled"; }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM birds WHERE lot_id=? LIMIT 1")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { c.rollback(); return "Cannot cancel a lot with bids"; } }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM deals WHERE lot_id=? LIMIT 1")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) { c.rollback(); return "Cannot cancel a lot with deals"; } }
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE lots SET status='CANCELLED' WHERE id=?")) {
                ps.setLong(1, lotId); ps.executeUpdate();
            }
            c.commit(); return "Auction cancelled";
        } catch (SQLException e) { rollback(c); return "Cancel failed: " + e.getMessage(); }
        finally { resetAutoCommit(c); }
    }

    public static String withdrawBid(long bidId) {
        Connection c = null;
        try {
            c = connection(); c.setAutoCommit(false);
            long lotId;
            try (PreparedStatement ps = c.prepareStatement("SELECT lot_id FROM birds WHERE id=? FOR UPDATE")) {
                ps.setLong(1, bidId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return "Bid not found"; } lotId = rs.getLong("lot_id");
                }
            }
            long oldPrice;
            try (PreparedStatement ps = c.prepareStatement("SELECT pprice,start_price,status FROM lots WHERE id=? FOR UPDATE")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"OPEN".equalsIgnoreCase(rs.getString("status"))) { c.rollback(); return "Bid can only be withdrawn from an open lot"; }
                    oldPrice = rs.getLong("start_price");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM birds WHERE id=?")) { ps.setLong(1, bidId); ps.executeUpdate(); }
            long nextPrice;
            try (PreparedStatement ps = c.prepareStatement("SELECT MAX(price) AS max_price FROM birds WHERE lot_id=?")) {
                ps.setLong(1, lotId); try (ResultSet rs = ps.executeQuery()) { rs.next(); long max = rs.getLong("max_price"); nextPrice = rs.wasNull() ? oldPrice : max; }
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE lots SET pprice=? WHERE id=?")) { ps.setLong(1, nextPrice); ps.setLong(2, lotId); ps.executeUpdate(); }
            c.commit(); return "Bid withdrawn; currentPrice=" + nextPrice;
        } catch (SQLException e) { rollback(c); return "Withdraw failed: " + e.getMessage(); }
        finally { resetAutoCommit(c); }
    }

    private static void rollback(Connection c) { if (c != null) try { c.rollback(); } catch (SQLException ignored) {} }
    private static void resetAutoCommit(Connection c) { if (c != null) try { c.setAutoCommit(true); } catch (SQLException ignored) {} }
}
