package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.application.CareHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import com.ihc.mascotas.backend.auth.domain.UserRepository;

@RestController
@RequestMapping("/api/care-history")
public class CareHistoryController {
    private final CareHistoryService careHistoryService;
    private final UserRepository users;

    public CareHistoryController(CareHistoryService careHistoryService, UserRepository users) {
        this.careHistoryService = careHistoryService;
        this.users = users;
    }

    @GetMapping
     public List<CareHistoryResponse> findByUser(Principal principal) {
        return careHistoryService.findByUserEmail(principal.getName()).stream().map(entry -> {
            String author = users.findById(entry.userId()).map(user -> user.fullName()).orElse("Usuario eliminado");
            return new CareHistoryResponse(entry.id(), entry.userId(), entry.careRecordId(), entry.petId(),
                    entry.petName(), entry.care(), entry.action(), entry.previousCareDate(), entry.currentCareDate(),
                    entry.occurredAt(), entry.changeReason(), author);
        }).toList();
    }
}