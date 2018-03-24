package com.github.enpassant.ickenham.springmvc;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

public class IckenhamViewResolver
    extends AbstractTemplateViewResolver
    implements InitializingBean
 {
     public IckenhamViewResolver() {
         setViewClass(requiredViewClass());
     }

     @Override
     protected Class<?> requiredViewClass() {
         return IckenhamView.class;
     }

     public void afterPropertiesSet() throws Exception {
         //getServletContext();
         if (getSuffix() == null || getSuffix().length() == 0) {
             super.setSuffix(".hbs");
         }
     }
}
