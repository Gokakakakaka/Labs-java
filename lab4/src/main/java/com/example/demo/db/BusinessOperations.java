package com.example.demo.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BusinessOperations {

    private BusinessOperations() {}

    public static Map<String, Object> publishItem(
            long userId, String about, String condition,
            long startingPrice, String step, String endDate) {

        Connection c = null;

        try {
            c = Db.connection();
            c.setAutoCommit(false);

            if (!Users.exists(c, userId)) {
                return fail(c, "Пользователь не найден");
            }

            Map<String, Object> item = Item.addItem(c, userId, about, condition);

            if (!ok(item)) {
                return fail(c, (String) item.get("message"));
            }

            long itemId = id(item);

            Map<String, Object> lot = Lots.addLot(
                    c, itemId, startingPrice, startingPrice,
                    step, endDate, "OPEN"
            );

            if (!ok(lot)) {
                return fail(c, (String) lot.get("message"));
            }

            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("item", item.get("data"));
            data.put("lot", lot.get("data"));

            return ApiResponse.success("Товар опубликован, лот создан", data);

        } catch (SQLException e) {
            rollback(c);
            return ApiResponse.error("Ошибка публикации: " + e.getMessage());

        } finally {
            reset(c);
        }
    }

    public static Map<String, Object> placeBid(
            long lotId, long userId, long price, String date) {

        Connection c = null;

        try {
            c = Db.connection();
            c.setAutoCommit(false);

            Map<String, Object> lot = Lots.find(c, lotId, true);

            if (lot == null) {
                return fail(c, "Лот не найден");
            }

            if (!"OPEN".equalsIgnoreCase(String.valueOf(lot.get("status")))) {
                return fail(c, "Лот не открыт");
            }

            if (!Users.exists(c, userId)) {
                return fail(c, "Пользователь не найден");
            }

            long current = ((Number) lot.get("pprice")).longValue();

            if (price <= current) {
                return fail(c, "Ставка должна превышать текущую цену " + current);
            }

            Map<String, Object> bid = Birds.addBird(c, lotId, userId, price, date);

            if (!ok(bid)) {
                return fail(c, (String) bid.get("message"));
            }

            Lots.updatePrice(c, lotId, price);
            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("bid", bid.get("data"));
            data.put("current_price", price);

            return ApiResponse.success("Ставка принята", data);

        } catch (SQLException e) {
            rollback(c);
            return ApiResponse.error("Ошибка ставки: " + e.getMessage());

        } finally {
            reset(c);
        }
    }

    public static Map<String, Object> closeAuction(long lotId) {

        Connection c = null;

        try {
            c = Db.connection();
            c.setAutoCommit(false);

            Map<String, Object> lot = Lots.find(c, lotId, true);

            if (lot == null) {
                return fail(c, "Лот не найден");
            }

            if (!"OPEN".equalsIgnoreCase(String.valueOf(lot.get("status")))) {
                return fail(c, "Лот не открыт");
            }

            Map<String, Object> bid = Birds.highestBird(c, lotId);

            if (bid == null) {
                return fail(c, "Нельзя закрыть аукцион без ставок");
            }

            long bidId = ((Number) bid.get("id")).longValue();
            long price = ((Number) bid.get("price")).longValue();
            long winner = ((Number) bid.get("user_id")).longValue();

            Map<String, Object> deal = Deals.addDeal(
                    c, lotId, bidId, price, "COMPLETED"
            );

            if (!ok(deal)) {
                return fail(c, (String) deal.get("message"));
            }

            Lots.updateStatus(c, lotId, "CLOSED", price);
            c.commit();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deal", deal.get("data"));
            data.put("winner_user_id", winner);
            data.put("price", price);

            return ApiResponse.success("Аукцион закрыт", data);

        } catch (SQLException e) {
            rollback(c);
            return ApiResponse.error("Ошибка закрытия аукциона: " + e.getMessage());

        } finally {
            reset(c);
        }
    }

    public static Map<String, Object> cancelAuction(long lotId) {

        Connection c = null;

        try {
            c = Db.connection();
            c.setAutoCommit(false);

            Map<String, Object> lot = Lots.find(c, lotId, true);

            if (lot == null) {
                return fail(c, "Лот не найден");
            }

            if (!"OPEN".equalsIgnoreCase(String.valueOf(lot.get("status")))) {
                return fail(c, "Отменить можно только открытый лот");
            }

            if (Birds.hasBids(c, lotId)) {
                return fail(c, "Нельзя отменить лот со ставками");
            }

            if (Deals.existsForLot(c, lotId)) {
                return fail(c, "Нельзя отменить лот со сделками");
            }

            Lots.updateStatus(c, lotId, "CANCELLED");
            c.commit();

            return ApiResponse.success(
                    "Аукцион отменён",
                    Map.of("lot_id", lotId, "status", "CANCELLED")
            );

        } catch (SQLException e) {
            rollback(c);
            return ApiResponse.error("Ошибка отмены аукциона: " + e.getMessage());

        } finally {
            reset(c);
        }
    }

    public static Map<String, Object> withdrawBid(long bidId, long currentUserId, boolean admin) {

        Connection c = null;

        try {
            c = Db.connection();
            c.setAutoCommit(false);
            Map<String, Object> bid = Birds.find(c, bidId, true);

            if (bid == null) {
                return fail(c, "Ставка не найдена");
            }

            long lotId = ((Number) bid.get("lot_id")).longValue();

            Map<String, Object> lot = Lots.find(c, lotId, true);

            if (lot == null ||
                    !"OPEN".equalsIgnoreCase(String.valueOf(lot.get("status")))) {
                return fail(c, "Ставку можно удалить только из открытого лота");
            }

            String owner = Birds.BirdOwner(c, lotId);

            if (owner == null) {
                return fail(c, "На лоте нет ставок");
            }

            long ownerId = Long.parseLong(owner);
            if (!admin && ownerId != currentUserId)fail(c,"Ты можешь отменять лишь свои ставки");

            long start = ((Number) lot.get("start_price")).longValue();

            Birds.delete(c, bidId);

            long next = Birds.maxPrice(c, lotId, start);

            Lots.updatePrice(c, lotId, next);
            c.commit();

            return ApiResponse.success(
                    "Ставка отозвана",
                    Map.of(
                            "bid_id", bidId,
                            "lot_id", lotId,
                            "current_price", next
                    )
            );

        } catch (SQLException e) {
            rollback(c);
            return ApiResponse.error("Ошибка отзыва ставки: " + e.getMessage());

        } finally {
            reset(c);
        }
    }


    private static boolean ok(Map<String, Object> r) {
        return r != null && Boolean.TRUE.equals(r.get("success"));
    }

    private static long id(Map<String, Object> r) {
        return ((Number) ((Map<?, ?>) r.get("data")).get("id")).longValue();
    }

    private static Map<String, Object> fail(Connection c, String message) {
        rollback(c);
        return ApiResponse.error(message);
    }

    private static void rollback(Connection c) {
        if (c != null) {
            try {
                c.rollback();
            } catch (SQLException ignored) {}
        }
    }

    private static void reset(Connection c) {
        if (c != null) {
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignored) {}
            try {
                c.close();
            } catch (SQLException ignored) {}
        }
    }
}
