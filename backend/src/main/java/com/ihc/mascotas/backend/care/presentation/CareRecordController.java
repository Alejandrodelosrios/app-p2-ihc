package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.application.CareRecordService;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/care-records")
public class CareRecordController {

    private final CareRecordService careRecordService;

    public CareRecordController(CareRecordService careRecordService) {
        this.careRecordService = careRecordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CareRecord create(
            Principal principal,
            @Valid @RequestBody CreateCareRecordRequest request
    ) {
        String email = principal.getName();

        return careRecordService.create(
                email,
                request.petName(),
                request.care(),
                request.animalType(),
                request.careDate()
        );
    }

    @GetMapping
    public List<CareRecord> findByEmail(
            Principal principal,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) CareRecordStatus status,
            @RequestParam(required = false) String careType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        String email = principal.getName();

        return careRecordService.findByUserEmail(
                email,
                petName,
                status,
                careType,
                fromDate,
                toDate
        );
    }

    @PatchMapping("/{id}/complete")
    public CareRecord complete(Principal principal, @PathVariable UUID id) {
        return careRecordService.complete(principal.getName(), id);
    }

    @PutMapping("/{id}")
    public CareRecord update(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCareRecordRequest request
    ) {
        return careRecordService.update(
                principal.getName(),
                id,
                request.petName(),
                request.care(),
                request.animalType(),
                request.careDate()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable UUID id) {
        careRecordService.delete(principal.getName(), id);
    }
}
