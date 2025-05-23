package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Client;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.service.CategoryService;
import com.SpringBootMVC.ExpensesTracker.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class MainController {
    ExpenseService expenseService;
    CategoryService categoryService;

    @Autowired
    public MainController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String landingPage(HttpSession session, Model model){
        Client client = (Client) session.getAttribute("client");
        model.addAttribute("sessionClient", client);
        return "landing-page";
    }

    @GetMapping("/showAdd")
    public String addExpense(Model model){
        model.addAttribute("expense", new ExpenseDTO());
        return "add-expense";
    }

    @PostMapping("/submitAdd")
    public String submitAdd(@ModelAttribute("expense") ExpenseDTO expenseDTO, HttpSession session){
        Client client = (Client) session.getAttribute("client");
        expenseDTO.setClientId(client.getId());
        expenseService.save(expenseDTO);
        return "redirect:/list";
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        Client client = (Client) session.getAttribute("client");
        int clientId = client.getId();
        List<Expense> expenseList = expenseService.findAllExpensesByClientId(clientId);

        for (Expense expense : expenseList) {
            if (expense.getCategory() != null) {
                expense.setCategoryName(expense.getCategory().getName());
            } else {
                expense.setCategoryName("Unknown Category"); // This indicates a problem
            }
        }

        model.addAttribute("expenseList", expenseList);
        model.addAttribute("filter", new FilterDTO());
        return "list-page";
    }

    @GetMapping("/showUpdate")
    public String showUpdate(@RequestParam("expId") int id, Model model) {
        Expense expense = expenseService.findExpenseById(id);
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setDateTime(expense.getDateTime());
        expenseDTO.setDescription(expense.getDescription());
        if (expense.getCategory() != null) {
            expenseDTO.setCategory(expense.getCategory().getName());
        } else {
            expenseDTO.setCategory("Unknown Category"); // This should not happen if the category is set correctly
        }
        model.addAttribute("expense", expenseDTO);
        model.addAttribute("expenseId", id);
        return "update-page";
    }

    @PostMapping("/submitUpdate")
    public String update(@RequestParam("expId") int id,
                         @ModelAttribute("expense") ExpenseDTO expenseDTO,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        Client client = (Client) session.getAttribute("client");
        expenseDTO.setExpenseId(id);
        expenseDTO.setClientId(client.getId());

        // Retrieve the category based on the selected name
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        if (category != null) {
            expenseService.update(expenseDTO);
            return "redirect:/list"; // Redirect to the list page on success
        } else {
            // Set an error message and redirect back to the update page
            redirectAttributes.addFlashAttribute("errorMessage", "Selected category not found. Please select a valid category.");
            return "redirect:/showUpdate?expId=" + id; // Redirect back to the update page with the expense ID
        }
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("expId") int id){
        expenseService.deleteExpenseById(id);
        return "redirect:/list";
    }


    @PostMapping("/processFilter")
    public String processFilter(@ModelAttribute("filter") FilterDTO filter, Model model){
        System.out.println("--------------------------------------------------------------");
        System.out.println("filter values : " + filter);
        List<Expense> expenseList = expenseService.findFilterResult(filter);
        System.out.println("size ----> " + expenseList.size());
        System.out.println(expenseList);

        for (Expense expense : expenseList){
            expense.setCategoryName(categoryService.findCategoryById(expense.getCategory().getId()).getName());
            expense.setDate(LocalDateTime.parse(expense.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate().toString());
            expense.setTime(LocalDateTime.parse(expense.getDateTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalTime().toString());
        }
        model.addAttribute("expenseList", expenseList);
        return "filter-result";
    }


}
