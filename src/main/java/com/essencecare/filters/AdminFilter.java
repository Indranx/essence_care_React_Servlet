package com.essencecare.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/api/admin/*")
public class AdminFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        System.out.println("AdminFilter: Checking admin access");
        HttpSession session = httpRequest.getSession(false);
        
        if (session != null) {
            String role = (String) session.getAttribute("role");
            System.out.println("AdminFilter: User role: " + role);
            
            if (role != null && role.equals("ADMIN")) {
                System.out.println("AdminFilter: Admin access granted");
                chain.doFilter(request, response);
                return;
            }
        }

        System.out.println("AdminFilter: Admin access denied");
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpResponse.getWriter().write("{\"error\": \"Admin access required\"}");
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
} 