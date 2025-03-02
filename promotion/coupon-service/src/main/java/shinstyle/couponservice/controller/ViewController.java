package shinstyle.couponservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @GetMapping("/ui/coupons")
    public String couponPage() {
        return "coupon";
    }

    @GetMapping("/ui/policies")
    public String policyPage() {
        return "policy";
    }
}
