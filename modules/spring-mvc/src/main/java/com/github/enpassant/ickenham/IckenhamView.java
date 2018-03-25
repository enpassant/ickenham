package com.github.enpassant.ickenham.springmvc;

import com.github.enpassant.ickenham.Ickenham;
import com.github.enpassant.ickenham.adapter.JavaAdapter;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.view.AbstractTemplateView;

public class IckenhamView extends AbstractTemplateView {
    private final Logger logger = LoggerFactory.getLogger(IckenhamView.class);
    private SpringIckenham springIckenham;

    @Override
    protected void renderMergedTemplateModel(
        Map<String, Object> model,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {
        springIckenham.render(model, response.getWriter(), request.getLocale());
    }

    @Override
    public boolean checkResource(Locale locale) throws Exception {
        return getUrl().endsWith(".hbs");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        springIckenham = new SpringIckenham(
            getUrl(),
            getServletContext(),
            getApplicationContext());
    }
}
