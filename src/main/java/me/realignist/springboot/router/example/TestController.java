package me.realignist.springboot.router.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    public void simpleAction() {

    }

    public @ResponseBody String sayHelloTo(@PathVariable(value = "name") String name) {
        return "Hello " + name + " !";
    }
}