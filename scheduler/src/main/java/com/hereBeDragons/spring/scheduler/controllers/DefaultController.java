package com.hereBeDragons.spring.scheduler.controllers;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping(value="/")
public class DefaultController {

    //private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @RequestMapping(value = {"/", "ndex"}, method = RequestMethod.GET, name = "Returns the index view")
    public String index(Model model, HttpServletRequest request) {
        return "Index";
    }

    @RequestMapping(value ="error", method = RequestMethod.GET, name="Default Error page")
    public String error(Model model, HttpServletRequest request){
        return "Error";
    }
}
