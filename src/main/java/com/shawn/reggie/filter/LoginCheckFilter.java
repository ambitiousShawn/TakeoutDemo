package com.shawn.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.shawn.reggie.common.BaseContext;
import com.shawn.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("过滤器执行了！");
        //转换请求和响应对象
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求的URI
        String requestURI = request.getRequestURI();

        //判断本次请求是否需要处理
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };

        boolean check = check(urls,requestURI);

        //如果不需要处理
        if (check){
            System.out.println("访问uri无需处理");
            filterChain.doFilter(request,response);
            return;
        }

        //客户端 处理登录
        Long id = (Long) request.getSession().getAttribute("employee");
        if(id != null){
            System.out.println("访问的需要处理，并且处理通过");

            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }

        //移动端 处理登录
        Long userId = (Long) request.getSession().getAttribute("user");
        if(userId != null){
            System.out.println("访问的需要处理，并且处理通过");

            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        //未登录状态
        //此处是根据前端代码写的后端，前端接收到NOTLOGIN字符串会自动跳转登陆页面。
        //此处也可以后端手动跳转登陆页面。
        System.out.println("访问数据需要处理但处理未通过");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 字符串路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url,requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
    @Override
    public void destroy() {

    }
}
