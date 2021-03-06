package com.alibabacloud.hipstershop.web;

import com.alibabacloud.hipstershop.Application;
import com.alibabacloud.hipstershop.CartItem;
import com.alibabacloud.hipstershop.dao.CartDAO;
import com.alibabacloud.hipstershop.dao.ProductDAO;
import com.alibabacloud.hipstershop.domain.Product;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

/**
 * @author wangtao 2019-08-12 15:41
 */
@Controller
public class AppController {


    private static String env = System.getenv("demo_version");

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CartDAO cartDAO;


    @Autowired
    private Registration registration;

    private Random random = new Random(System.currentTimeMillis());

    private String userID = "Test User";

    public static String PRODUCT_APP_NAME = "";
    public static String PRODUCT_SERVICE_TAG = "";
    public static String PRODUCT_IP = "";


    @ModelAttribute
    public void setVaryResponseHeader(HttpServletResponse response) {
        response.setHeader("APP_NAME", Application.APP_NAME);
        response.setHeader("SERVICE_TAG", Application.SERVICE_TAG);
        response.setHeader("SERVICE_IP", registration.getHost());
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("products", productDAO.getProductList());

        model.addAttribute("FRONTEND_APP_NAME", Application.APP_NAME);
        model.addAttribute("FRONTEND_SERVICE_TAG", Application.SERVICE_TAG);
        model.addAttribute("FRONTEND_IP", registration.getHost());

        model.addAttribute("PRODUCT_APP_NAME", PRODUCT_APP_NAME);
        model.addAttribute("PRODUCT_SERVICE_TAG", PRODUCT_SERVICE_TAG);
        model.addAttribute("PRODUCT_IP", PRODUCT_IP);

        model.addAttribute("new_version", StringUtils.isBlank(env));
        return "index.html";
    }

    @GetMapping("/setExceptionByIp")
    public String setExceptionByIp(@RequestParam(name="ip", required=false, defaultValue="") String ip, Model model) {
        try {
            OutlierController.allNum++;
            productDAO.setExceptionByIp(ip);
        } catch (Exception e) {
            OutlierController.exceptionNum++;
            throw e;
        }
        model.addAttribute("products", productDAO.getProductList());
        return "index.html";
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "home";
    }

    @GetMapping("/setUser")
    public String user(@RequestParam(name="userId", required=false) String userId, Model model) {
        userID = userId;
        return "index.html";
    }


    @GetMapping("/exception")
    public String exception() {

        throw new RuntimeException();
    }

    @GetMapping("/exception2")
    public String excpetion2(){

        int i = 20 / 0;
        return "hello";
    }

    @GetMapping("/checkout")
    public String checkout() {

        if(random.nextBoolean()){
            throw new RuntimeException();
        }

        return "not support yet";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable(name="id") String id, Model model) {
        Product p = productDAO.getProductById(id);
        model.addAttribute("product", p);
        return "product.html";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        List<CartItem> items = cartDAO.viewCart(userID);
        for (CartItem item: items) {
            Product p = productDAO.getProductById(item.productID);
            item.productName = p.getName() + item.productName;
            item.price = p.getPrice();
            item.productPicture = p.getPicture();
        }
        model.addAttribute("items", items);
        return "cart.html";
    }

    @PostMapping("/cart")
    public RedirectView addToCart(@RequestParam(name="product_id") String productID,
                                  @RequestParam(name="quantity") int quantity) {
        cartDAO.addToCart(userID, productID, quantity);
        return new RedirectView("/cart");
    }

    @ModelAttribute("cartSize")
    public int getCartSize() {
        return cartDAO.viewCart(userID).size();
    }

}
