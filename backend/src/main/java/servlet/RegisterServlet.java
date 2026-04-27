package servlet;

import dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            boolean created = userDAO.createUser(username, email, password);

            if (created) {
                response.getWriter().write("REGISTER_SUCCESS");
            } else {
                response.getWriter().write("USERNAME_OR_EMAIL_TAKEN");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("REGISTER_ERROR");
        }
    }
}