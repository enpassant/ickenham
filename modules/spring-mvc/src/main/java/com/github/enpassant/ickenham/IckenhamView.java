package com.github.enpassant.ickenham.springmvc;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.view.AbstractTemplateView;

public class IckenhamView extends AbstractTemplateView {
    final Logger logger = LoggerFactory.getLogger(IckenhamView.class);

    @Override
    protected void renderMergedTemplateModel(
        Map<String, Object> model,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {
        logger.debug("Url: {}, model: {}", getUrl(), model);
	//WebEngine.setRequestAndResponse(request, response);
        //WebEngine.getEngine().getTemplate(getUrl(), request.getLocale(), model).render(model, response);
        //getUrl(), request.getLocale(), model).render(model, response);
    }

    @Override
    public boolean checkResource(Locale locale) throws Exception {
        //getServletContext();
        //return hasResource(getUrl(), locale);
        return getUrl().endsWith(".hbs");
    }
}
