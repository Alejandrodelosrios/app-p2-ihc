package com.ihc.mascotas.backend.care.application;

import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import com.ihc.mascotas.backend.care.domain.CareHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareHistoryService {
    private final CareHistoryRepository careHistoryRepository;
    private final UserRepository userRepository;

    public CareHistoryService(CareHistoryRepository careHistoryRepository, UserRepository userRepository) {
        this.careHistoryRepository = careHistoryRepository;
        this.userRepository = userRepository;
    }

    public List<CareHistoryEntry> findByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return careHistoryRepository.findByUserIdOrderByOccurredAtDesc(user.id());
    }
}
