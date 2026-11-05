package com.ihc.mascotas.backend.care.presentation;

import com.ihc.mascotas.backend.care.application.CareRecordService;
import com.ihc.mascotas.backend.care.application.CareRecordView;
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
    public CareRecordResponse create(
            Principal principal,
            @Valid @RequestBody CreateCareRecordRequest request
    ) {
        return toResponse(careRecordService.create(
                principal.getName(),
                request.petId(),
                request.care(),
                request.careDate(),
                request.assigneeEmail(),
                request.recurrenceIntervalDays()
        ));
    }

    @GetMapping
    public List<CareRecordResponse> findByEmail(
            Principal principal,
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) CareRecordStatus status,
            @RequestParam(required = false) String careType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return careRecordService.findByUserEmail(
                        principal.getName(),
                        petId,
                        status,
                        careType,
                        fromDate,
                        toDate
                )
                .stream()
                .map(CareRecordController::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/complete")
    public CareRecordResponse complete(Principal principal, @PathVariable UUID id) {
        return toResponse(careRecordService.complete(principal.getName(), id));
    }

    @PutMapping("/{id}")
    public CareRecordResponse update(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCareRecordRequest request
    ) {
        return toResponse(careRecordService.update(
                principal.getName(),
                id,
                request.petId(),
                request.care(),
                request.careDate(),
                request.assigneeEmail(),
                request.recurrenceIntervalDays()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable UUID id) {
        careRecordService.delete(principal.getName(), id);
    }

    private static CareRecordResponse toResponse(CareRecordView view) {
        CareRecord record = view.record();
        return new CareRecordResponse(
                record.id(),
                record.userId(),
                record.petId(),
                view.petName(),
                view.petSpecies(),
                view.petActive(),
                record.care(),
                record.careDate(),
                record.createdAt(),
                record.status(),
                record.assignedToUserId(),
                record.recurrenceIntervalDays()
        );
    }
}