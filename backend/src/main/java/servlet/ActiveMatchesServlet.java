package servlet;

import server.GameHub;
import server.GameHub.ActiveMatch;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/active-matches")
public class ActiveMatchesServlet extends HttpServlet {
    private final GameHub hub = GameHub.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ActiveMatch> matches = hub.getActiveMatchesSnapshot();

        response.setContentType("application/json");
        response.getWriter().write(buildJson(matches));
    }

    private String buildJson(List<ActiveMatch> matches) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"matches\":[");
        for (int i = 0; i < matches.size(); i++) {
            ActiveMatch match = matches.get(i);
            sb.append("{");
            sb.append("\"gameId\":\"").append(escape(match.gameId())).append("\",");
            sb.append("\"player1\":\"").append(escape(match.player1())).append("\",");
            sb.append("\"player2\":\"").append(escape(match.player2())).append("\"");
            sb.append("}");
            if (i < matches.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

