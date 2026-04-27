package servlet;

import dao.UserDAO;
import model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            User user = userDAO.validateLogin(username, password);

            if (user == null) {
                response.getWriter().write("LOGIN_FAILED");
                return;
            }

            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("elo", user.getEloRating());

            response.getWriter().write("LOGIN_SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("LOGIN_ERROR");
        }
    }
}