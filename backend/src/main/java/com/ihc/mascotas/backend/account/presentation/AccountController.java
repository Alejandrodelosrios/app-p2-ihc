package com.ihc.mascotas.backend.account.presentation;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Locale;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ihc.mascotas.backend.account.application.AccountData;
import com.ihc.mascotas.backend.account.application.AccountDataCsv;
import com.ihc.mascotas.backend.account.application.AccountService;
import com.ihc.mascotas.backend.account.application.DeletionSummary;
import com.ihc.mascotas.backend.shared.exception.NotAllowedException;

@RestController
@RequestMapping("/api/me")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/datos")
    public ResponseEntity<Object> data(
            Principal principal,
            @RequestParam(name = "formato", defaultValue = "json") String formato) {
        String format = formato.toLowerCase(Locale.ROOT);
        if (!format.equals("json") && !format.equals("csv")) {
            throw new NotAllowedException("Formato no válido. Usa json o csv.");
        }

        AccountData data = accountService.exportData(principal.getName());
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("mis-datos-mascotas-al-dia." + format).build().toString());

        if (format.equals("csv")) {
            return response
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(AccountDataCsv.toCsv(data));
        }
        return response.contentType(MediaType.APPLICATION_JSON).body(data);
    }

    @GetMapping("/eliminacion/resumen")
    public DeletionSummary deletionSummary(Principal principal) {
        return accountService.deletionSummary(principal.getName());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(Principal principal) {
        accountService.deleteAccount(principal.getName());
    }
}