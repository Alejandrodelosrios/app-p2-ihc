package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.application.CareHistoryService;
import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/care-history")
public class CareHistoryController {
    private final CareHistoryService careHistoryService;

    public CareHistoryController(CareHistoryService careHistoryService) {
        this.careHistoryService = careHistoryService;
    }

    @GetMapping
    public List<CareHistoryEntry> findByUser(Principal principal) {
        return careHistoryService.findByUserEmail(principal.getName());
    }
}
