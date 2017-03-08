/*
Copyright 2009-2016 Igor Polevoy

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
package org.javalite.activeweb;


import org.javalite.common.Convert;
import org.javalite.test.jspec.TestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static org.javalite.common.Util.blank;

/**
 * This class is not used directly in applications.
 *
 * @author Igor Polevoy
 */
public class RequestSpecHelper extends SpecHelper{

    @Before
    public final void atStart00(){
        Configuration.setTesting(true);
    }
    /**
     * Provides status code set on response by controller
     *
     * @return  status code set on response by controller
     */
    protected int statusCode(){
        return RequestContext.getControllerResponse().getStatus();
    }

    /**
     * Provides content type set on response by controller
     *
     * @return  content type set on response by controller
     */
    protected String contentType(){
        return RequestContext.getControllerResponse().getContentType();
    }

    /**
     * Provides content generated by controller after controller execution - if views were integrated.
     *
     * @return content generated by controller/view
     */
    protected String responseContent(){
        try{
            Boolean integrateViews = (Boolean) RequestContext.getRequestVo().get("integrateViews");

            //content can be provided simply by respond() method
            String content = ((MockHttpServletResponse) RequestContext.getHttpResponse()).getContentAsString();

            if(integrateViews == null){
                throw new RuntimeException("Are you sure you terminated the request() with post(), get(), etc. method?");
            }

            if(!integrateViews && blank(content)){
                throw new SpecException("Use integrateViews() method to generate response content");
            }
            return content;
        }
        catch(SpecException e){
            throw e;
        }
        catch(Exception e){
            throw new SpecException(e);
        }
    }

    /**
     * Provides content generated by controller as bytes.
     *
     * @return byte array as it was written from controller.
     */
    protected byte[] bytesContent(){
        try{
            return ((MockHttpServletResponse) RequestContext.getHttpResponse()).getContentAsByteArray();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides layout set after executing an action of a controller.
     *
     * @return  layout set after executing an action of a controller.
     */
    protected String layout(){
        ControllerResponse resp = RequestContext.getControllerResponse();
        try{
            if(!(resp instanceof RenderTemplateResponse))
                throw new SpecException("failed to get layout, did you perform a render operation? I found a different " +
                        " response: " + resp.getClass());
            return ((RenderTemplateResponse)resp).getLayout();
        }catch(ClassCastException e){
            return null;
        }
    }


    protected String template(){
        ControllerResponse resp = RequestContext.getControllerResponse();
        try{
            if(!(resp instanceof RenderTemplateResponse))
                throw new SpecException("failed to get layout, did you perform a render operation? I found a different " +
                        " response: " + resp.getClass());
            return ((RenderTemplateResponse)resp).getTemplate();
        }catch(ClassCastException e){
            return null;
        }
    }

    /**
     * Provides values assigned by controller during execution. These values will
     *
     * be forwarded to a view during normal processing.
     *
     * @return values assigned by controller during execution
     */
    protected Map assigns(){
        if(RequestContext.getControllerResponse() == null){
            throw new TestException("There is no controller response, did you actually invoke a controller/action?");
        }
        return RequestContext.getControllerResponse().values();
    }

    /**
     * Synonym of {@link #assigns()}.
     *
     * @return values assigned by controller during execution
     */
    protected Map vals(){
        if(RequestContext.getControllerResponse() == null){
            throw new TestException("There is no controller response, did you actually invoke a controller/action?");
        }
        return RequestContext.getControllerResponse().values();
    }

    /**
     * Returns a single value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     *
     * @return a single value assigned by controller.
     */
    protected Object val(String name){
        if(RequestContext.getControllerResponse() == null){
            throw new TestException("There is no controller response, did you actually invoke a controller/action?");
        }
        Object val = RequestContext.getControllerResponse().values().get(name);
        return val == null? RequestContext.getHttpRequest().getAttribute(name): val;
    }

    /**
     * Returns a single value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     *
     * @param type type to be returned.
     *
     * @return a single value assigned by controller.
     */
    protected  <T>  T val(String name, Class<T> type){
        if(RequestContext.getControllerResponse() == null){
            throw new TestException("There is no controller response, did you actually invoke a controller/action?");
        }
        return (T) RequestContext.getControllerResponse().values().get(name);
    }

    /**
     * Returns header set by controller or filter.
     *
     * @param headerName name of header
     * @return header value (can be null).
     */
    protected String header(String headerName){
        return RequestContext.getHttpResponse().getHeader(headerName);
    }

    /**
     * Returns all headers set by controller or filter.
     * If a header has more than  one value, only one value is returned.
     *
     * @return map of headers, where keys are names of headers, and values are header values.
     */
    protected Map<String, String> headers(){
        Collection<String> headerNames =  RequestContext.getHttpResponse().getHeaderNames();
        Map<String, String> headers= new HashMap<>();
        for(String name: headerNames){
            headers.put(name, header(name));
        }
        return headers;
    }

    /**
     * Returns all headers set by controller or filter.
     * If a header has more than  one value, only one is returned.
     *
     * @return map of headers, where keys are names of headers, and values are header values.
     */
    protected List<String> headerNames(){
        return new ArrayList<>(RequestContext.getHttpResponse().getHeaderNames());
    }

    /**
     * String value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected String valString(String name){
        assert name != null;
        return val(name).toString();
    }

    /**
     * int value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected int valInteger(String name){
        assert name != null;
        return Convert.toInteger(val(name));
    }

    /**
     * long value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected long valLong(String name){
        assert name != null;
        return Convert.toLong(val(name));
    }

    /**
     * double value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected double valDouble(String name){
        assert name != null;
        return Convert.toDouble(val(name));
    }

    /**
     * float value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected float valFloat(String name) {
        assert name != null;
        return Convert.toFloat(val(name));
    }

    /**
     * boolean value assigned by controller.
     *
     * @param name name of a value assigned by controller.
     * @return a single value assigned by controller.
     */
    protected boolean valBoolean(String name){
        assert name != null;
        return Convert.toBoolean(val(name));
    }

    /**
     * Returns true after execution of an action that sent a redirect.
     * @return true after execution of an action that sent a redirect, false otherwise.
     */
    protected boolean redirected(){
        return RequestContext.getControllerResponse() instanceof RedirectResponse;
    }

    /**
     * Returns a redirect value if one was produced by a controller or filter, null if not.
     *
     * @return a redirect value if one was produced by a controller or filter, null if not.
     */
    protected String redirectValue(){
        ControllerResponse resp = RequestContext.getControllerResponse();
        if(resp != null && resp instanceof RedirectResponse){
            RedirectResponse redirectResponse = (RedirectResponse)resp;
            return redirectResponse.redirectValue();
        }
        return null;
    }

    /**
     * Returns all cookies from last response. Use in test validations.
     *
     * @return all cookies from last response.
     */
    protected Cookie[] getCookies(){
        if(RequestContext.getHttpResponse() == null) throw new IllegalStateException("response does not exist");
        javax.servlet.http.Cookie[] servletCookies = ((MockHttpServletResponse) RequestContext.getHttpResponse()).getCookies();
        List<Cookie> cookies = new ArrayList<>();
        for(javax.servlet.http.Cookie cookie: servletCookies){
            cookies.add(Cookie.fromServletCookie(cookie));
        }
        return cookies.toArray(new Cookie[0]);
    }

    /**
     * Returns a cookie from last response by name, <code>null</code> if not found.
     * @param name name of cookie.
     * @return a cookie from last response by name, <code>null</code> if not found.
     */
    protected Cookie cookie(String name){
        Cookie[] cookies = getCookies();
        for(Cookie cookie: cookies){
            if(cookie.getName().equals(name)){
                return cookie;
            }
        }
        return null;
    }

    /**
     * Convenience method, returns cookie value.
     *
     * @param name name of cookie.
     * @return cookie value.
     */
    protected String cookieValue(String name){
        return cookie(name).getValue();
    }


    /**
     * Parses controller response content and selects content of HTML element using CSS selectors.<br>
     * <strong>Example:</strong><br>
     *
     *  <pre>
     *     request().get("index");
     *     a(find("div[class='greeting']").shouldEqual("Hello!");
     *  </pre>
     *
     * @param cssQuery CSS query. Implementation is based on <a href="http://jsoup.org/">JSoup</a>.
     * @return contents of selected element as text.
     */
    protected String text(String cssQuery){
        Document doc = Jsoup.parse(responseContent().replace("'", "\"")); //TODO: this replacement can be removed
        // when this PL is accepted: https://github.com/jhy/jsoup/pull/655 and a new JSoup version released
        Elements elements  = doc.select(cssQuery);
        if(elements.isEmpty()){
            return null;
        }else if(elements.size() > 1){
            throw new IllegalArgumentException("Your query points to multiple elements. Choose only one.");
        }

        return elements.get(0).text();
    }

    /**
     * Parses controller response and counts elements that are found by a CSS query.
     * <strong>Example:</strong><br>
     *
     *  <pre>
     *     request().get("index");
     *     a(count("div[class='main']").shouldEqual(1);
     *  </pre>

     * @param cssQuery CSS query. Implementation is based on <a href="http://jsoup.org/">JSoup</a>.
     * @return number of elements in HTML document that were matching the query.
     */
    public int count(String cssQuery) {
        return Jsoup.parse(responseContent().replace("'", "\"")).select(cssQuery).size();
    }
}
