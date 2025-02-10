package org.darexapp.web.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.darexapp.transaction.model.Transaction;
import org.darexapp.user.model.User;
import org.darexapp.user.service.UserService;
import org.darexapp.wallet.service.WalletService;
import org.darexapp.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public TransferController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    /**
     * GET endpoint за показване на страницата за трансфер.
     * Използва сесията за извличане на текущия потребител.
     */
    @GetMapping
    public ModelAndView showTransferPage(HttpSession session) {
        UUID userId = (UUID) session.getAttribute("user_id");
        User currentUser = userService.findById(userId);

        ModelAndView mav = new ModelAndView("transfers"); // transfer.html
        mav.addObject("user", currentUser);
        // Създаваме празен TransferRequest чрез builder-а
        mav.addObject("transferRequest", TransferRequest.builder().build());
        return mav;
    }

    /**
     * POST endpoint за обработка на трансфер формуляра.
     * Ако има валидационни грешки, връща отново формуляра.
     * При успешен трансфер – извиква walletService.transferFunds(...) и пренасочва към страницата с транзакция.
     */
    @PostMapping("/new")
    public ModelAndView initiateTransfer(@Valid @ModelAttribute("transferRequest") TransferRequest transferRequest,
                                         BindingResult bindingResult, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("user_id");
        User currentUser = userService.findById(userId);

        if (bindingResult.hasErrors()) {
            ModelAndView mav = new ModelAndView("create-transfer");
            mav.addObject("user", currentUser);
            mav.addObject("transferRequest", transferRequest);
            return mav;
        }

        // Извикване на walletService за извършване на трансфера
        Transaction transaction = walletService.transferFunds(currentUser, transferRequest);

        // Пренасочване към страницата с детайлите на новосъздадената транзакция
        return new ModelAndView("redirect:/transactions/" + transaction.getId());
    }


}
