/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/
package org.javalite.activeweb.freemarker;

import org.javalite.activeweb.TemplateManager;
import org.javalite.activeweb.ViewException;
import org.javalite.activeweb.ViewMissingException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.javalite.common.Util;
import org.javalite.activeweb.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Igor Polevoy
 */
public class FreeMarkerTemplateManager implements TemplateManager {

    private Configuration config;
    private String defaultLayout;

    private String location;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public FreeMarkerTemplateManager() {
        config = new Configuration();
        config.setObjectWrapper(new DefaultObjectWrapper());
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setSharedVariable("link_to", new LinkToTag());
        config.setSharedVariable("form", new FormTag());
        config.setSharedVariable("content", new ContentForTag());
        config.setSharedVariable("yield", new YieldTag());
        config.setSharedVariable("flash", new FlashTag());
        config.setSharedVariable("render", new RenderTag());
        config.setSharedVariable("confirm", new ConfirmationTag());
        config.setSharedVariable("wrap", new WrapTag());
        config.setSharedVariable("debug", new DebugTag());

        AbstractFreeMarkerConfig freeMarkerConfig = org.javalite.activeweb.Configuration.getFreeMarkerConfig();
        if(freeMarkerConfig != null){
            freeMarkerConfig.setConfiguration(config);
            freeMarkerConfig.init();
        }
    }

    public void merge(Map values, String template, Writer writer) {
        merge(values, template, defaultLayout, writer);
    }

    public void merge(Map input, String template, String layout, Writer writer) {

        //TODO: refactor this, add tests

        try {

            if(org.javalite.activeweb.Configuration.getEnv().equals("development")){
                config.clearTemplateCache();
            }

            ContentTL.reset();

            if(layout == null){//no layout
                Template temp = config.getTemplate(template + ".ftl");
                temp.process(input, writer);
                writer.flush();
                return;
            }



            //Generate the template itself
            Template pageTemplate = config.getTemplate(template + ".ftl");
            StringWriter pageWriter = new StringWriter();
            pageTemplate.process(input, pageWriter);
            String pageContent = pageWriter.toString();

            Map values = new HashMap(input);
            values.put("page_content", pageContent);
            Map<String, List<String>>  assignedValues = ContentTL.getAllContent();

            for(String name: assignedValues.keySet()){
                values.put(name, Util.join(assignedValues.get(name), " "));
            }

            Template layoutTemplate = config.getTemplate(layout + ".ftl");
            layoutTemplate.process(values, writer);

            FreeMarkerTL.setEnvironment(null);
            writer.flush();
            logger.info("Rendered template: '" + template + "' with layout: '" + layout + "'");

        }
        catch(FileNotFoundException e){
            throw new ViewMissingException(e);
        }
        catch(ViewException e){
            throw e;
        }
        catch (Exception e) {
            throw new ViewException("failed to render template: '" +(location != null? location:"") +  template + ".ftl" + (layout == null? "', without layout" : "', with layout: '" +(location != null? location:"") + layout + "'"), e);
        }
    }
    
    public void setServletContext(ServletContext ctx) {
        if(location == null)
            config.setServletContextForTemplateLoading(ctx, "WEB-INF/views/");
    }

    /**
     * This method exists for testing.
     *
     * @param path path to directory with test templates.
     * @throws IOException exception if directory not present.
     */
    public void setTemplateClassPath(String path) throws IOException {        
        config.setClassForTemplateLoading(this.getClass(), path);
    }

    public void setDefaultLayout(String layoutPath) {
        defaultLayout = layoutPath;
    }



    public void setTemplateLocation(String templateLocation) {
        location = templateLocation;

        try{
            config.setDirectoryForTemplateLoading(new File(templateLocation));
        }
        catch(Exception e){throw new InitException(e);}
    }



    /**
     * Registers an application-specific tag.
     *
     * @param name name of tag.
     * @param tag tag instance.
     */
    public void registerTag(String name, FreeMarkerTag tag){
       config.setSharedVariable(name, tag);
    }
}