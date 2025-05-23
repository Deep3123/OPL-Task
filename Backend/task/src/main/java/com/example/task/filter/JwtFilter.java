package com.example.task.filter;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.task.service.impl.CustomUserDetailsService;
import com.example.task.util.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private CustomUserDetailsService custom_user;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		try {
			String token = jwtService.getTokenFromRequest(request);

			if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				token = token.substring(7);
				String username = jwtService.extractUserName(token);

				UserDetails user = custom_user.loadUserByUsername(username);

				if (user != null && jwtService.validateToken(token, user)) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
							user.getAuthorities());
					auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(auth);

				}

			}
		} catch (Throwable e) {
//			System.out.println("Error Logs "+e.getMessage());
//			throw e;
		}
		filterChain.doFilter(request, response);
	}
}
