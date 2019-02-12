package com.hereBeDragons.spring.template.controller;


import com.hereBeDragons.spring.scheduler.controllers.DefaultController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultControllerTest {

    private DefaultController controller;

    @Before
    public void prepare() {
        controller = new DefaultController();
    }

    @Test
    public void indexTest() {
        Assert.assertEquals("Index", controller.index(null, null));
    }

    @Test
    public void errorTest() {
        Assert.assertEquals("Error", controller.error(null, null));
    }
}
