package com.aplana.timesheet.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: eyaroslavtsev
 * Date: 06.08.12
 * Time: 14:10
 * To change this template use File | Settings | File Templates.
 */
public class AutentificationFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        HttpSession session = request.getSession(false);
        try {
            String s = request.getParameter("j_username");
            session.setAttribute("lastLogin", s);
        } catch (Exception ex) {
            session.setAttribute("lastLogin", "");
        }
        chain.doFilter(req, resp);

    }

    public void init(FilterConfig config) throws ServletException {

    }

}
