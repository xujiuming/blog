package com.ming.base.web;

/**
 * @author ming
 * @date 2018-08-28 09:47:18
 */
public abstract class BaseAbstractController implements BaseControllerInterface {


    @Override
    public String redirect(String uri) {
        if (uri.startsWith("/")) {
            return "redirect:" + uri;
        } else {
            return "redirect:/" + uri;
        }
    }
}
