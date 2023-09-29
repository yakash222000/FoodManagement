package net.raofin.bearburger.controller;

import net.raofin.bearburger.model.Comment;
import net.raofin.bearburger.model.User;
import net.raofin.bearburger.service.CommentService;
import net.raofin.bearburger.service.FoodService;
import net.raofin.bearburger.service.UserService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.security.Principal;

@Controller
//@RequestMapping("/customer")
public class CustomerController
{
    private final UserService userService;
    private final FoodService foodService;
    private final CommentService commentService;

    public CustomerController(UserService userService,
                              FoodService foodService,
                              CommentService commentService) {
        this.userService = userService;
        this.foodService = foodService;
        this.commentService = commentService;
    }

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        webDataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @RequestMapping(value = "/profile", method = {RequestMethod.GET, RequestMethod.POST})
    public String showProfilePage(Principal principal, Model model) {

        User user = userService.fetchUserByUsername(principal.getName());
        model.addAttribute("user", user);

        return "customer/Profile";
    }

    @RequestMapping(value = "/profile-modify", method = {RequestMethod.GET, RequestMethod.POST})
    public String showProfileModifyPage(Principal principal, Model model) {

        User user = userService.fetchUserByUsername(principal.getName());
        user.setPassword("");
        model.addAttribute("user", user);

        return "customer/ProfileModify";
    }

    @RequestMapping("/profile-modify-action")
    public String profileModifyAction(@Valid @ModelAttribute("user") User updatedUser,
                                      BindingResult bindingResult,
                                      Principal principal) {

        if (bindingResult.hasFieldErrors("email") || bindingResult.hasFieldErrors("password")
                || bindingResult.hasFieldErrors("phoneNumber")) {
            return "redirect:/profile-modify?error";
        }

        User user = userService.fetchUserByUsername(principal.getName());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        userService.updateUser(user);

        return "redirect:/profile?updated";
    }

    @RequestMapping(value = "/payment", method = {RequestMethod.GET, RequestMethod.POST})
    public String showPaymentPage(@RequestParam("foodId") int foodId,
                                  Model model,
                                  HttpSession session) {

        model.addAttribute("food", foodService.findById(foodId));
        session.setAttribute("price", foodService.findById(foodId).getPrice());

        return "customer/Payment";
    }

    @GetMapping("/fetchUserById/{id}")
    User fetchUserById(@PathVariable String id) {
        return userService.fetchUserById(Integer.parseInt(id));
    }


    @RequestMapping(value = "/comments/{foodID}", method = {RequestMethod.GET, RequestMethod.POST})
    public String selectFoodID(@PathVariable("foodID") int foodID,
                               HttpSession session) {

        session.setAttribute("foodID", foodID);
        return "redirect:/comments";
    }

    @RequestMapping(value = "/comments", method = {RequestMethod.GET, RequestMethod.POST})
    public String showCommentPage(Model model, HttpSession session) {

        int foodID = session.getAttribute("foodID") == null ? 1 : (int) session.getAttribute("foodID");
        model.addAttribute("food", foodService.findById(foodID));

        return "customer/Comments";
    }

    @RequestMapping(value = "/post-comments")
    public String postComments(@RequestParam("commentID") int commentID,
                               @RequestParam("comment") String postedComment,
                               Principal principal,
                               HttpSession session) {

        Comment comment = new Comment();

        int foodID = session.getAttribute("foodID") == null ? 1 : (int) session.getAttribute("foodID");

        comment.setComment(postedComment);
        comment.setPostedBy(principal.getName());
        comment.setFoodID(foodID);
        comment.setParentID(commentID);

        commentService.addComment(comment);

        return "redirect:/comments?posted";
    }
}