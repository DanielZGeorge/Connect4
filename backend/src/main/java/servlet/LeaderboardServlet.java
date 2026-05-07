package servlet;

import dao.UserDAO;
import model.User;
import server.GameHub;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/leaderboard")
public class LeaderboardServlet extends HttpServlet {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final UserDAO userDAO = new UserDAO();
    private final GameHub hub = GameHub.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = parseInt(request.getParameter("page"), 1);
        int size = parseInt(request.getParameter("size"), DEFAULT_SIZE);
        if (size > MAX_SIZE) size = MAX_SIZE;
        if (page < 1) page = 1;

        String query = request.getParameter("q");
        String search = (query == null || query.isBlank()) ? "%" : "%" + query.trim() + "%";
        int offset = (page - 1) * size;

        try {
            List<User> users = userDAO.getLeaderboard(search, size, offset);
            int total = userDAO.getLeaderboardCount(search);

            response.setContentType("application/json");
            response.getWriter().write(buildJson(page, size, total, users));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"LEADERBOARD_ERROR\"}");
        }
    }

    private String buildJson(int page, int size, int total, List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"page\":").append(page).append(",");
        sb.append("\"size\":").append(size).append(",");
        sb.append("\"total\":").append(total).append(",");
        sb.append("\"players\":[");
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            boolean inMatch = hub.isPlayerInMatch(user.getUsername());
            sb.append("{");
            sb.append("\"username\":\"").append(escape(user.getUsername())).append("\",");
            sb.append("\"elo\":").append(user.getEloRating()).append(",");
            sb.append("\"inMatch\":").append(inMatch);
            sb.append("}");
            if (i < users.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

