package decimal.apigateway.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component
//@Order(1)
public class RequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        System.out.println(httpServletRequest.getRequestURL());

        MutableHttpServletRequest mutableHttpServletRequest = new MutableHttpServletRequest(httpServletRequest);

        mutableHttpServletRequest.putHeader("mapping", "logs/engine/v1/support");

        filterChain.doFilter(mutableHttpServletRequest, httpServletResponse);
    }
}
