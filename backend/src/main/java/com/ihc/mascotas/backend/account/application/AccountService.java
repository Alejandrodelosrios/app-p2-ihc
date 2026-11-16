package com.ihc.mascotas.backend.account.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ihc.mascotas.backend.analytics.application.AnalyticsService;
import com.ihc.mascotas.backend.analytics.domain.AnalyticsEvent;
import com.ihc.mascotas.backend.auth.domain.PasswordResetTokenRepository;
import com.ihc.mascotas.backend.auth.domain.User;
import com.ihc.mascotas.backend.auth.domain.UserRepository;
import com.ihc.mascotas.backend.care.domain.CareHistoryEntry;
import com.ihc.mascotas.backend.care.domain.CareHistoryRepository;
import com.ihc.mascotas.backend.care.domain.CareRecord;
import com.ihc.mascotas.backend.care.domain.CareRecordRepository;
import com.ihc.mascotas.backend.care.domain.CareRecordStatus;
import com.ihc.mascotas.backend.pet.domain.Pet;
import com.ihc.mascotas.backend.pet.domain.PetRepository;
import com.ihc.mascotas.backend.shared.exception.NotFoundException;

/**
 * "Mis datos" y eliminación de cuenta.
 *
 * Regla de Mascota al Día al eliminar la cuenta:
 *  - Los accesos se revocan: los cuidados pendientes que le asignaron quedan sin asignar.
 *  - Los cuidados que creó y asignó a otras personas siguen visibles para ellas.
 *  - El historial y los cuidados que realizó se conservan con su nombre como autor, para que
 *    ante un reclamo se sepa quién hizo cada cuidado.
 *  - Sus mascotas se desactivan y sus cuidados pendientes sin asignar se eliminan.
 *  - La fila del usuario se anonimiza (no se borra): se pierden el correo y la contraseña, se
 *    conserva el nombre. La cuenta ya no puede iniciar sesión.
 */
@Service
public class AccountService {

    static final String UNKNOWN_PET = "(mascota no disponible)";

    private final UserRepository users;
    private final PetRepository pets;
    private final CareRecordRepository careRecords;
    private final CareHistoryRepository history;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticsService analytics;
    private final Clock clock;

    @Autowired
    public AccountService(
            UserRepository users,
            PetRepository pets,
            CareRecordRepository careRecords,
            CareHistoryRepository history,
            PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwordEncoder,
            AnalyticsService analytics) {
        this(users, pets, careRecords, history, resetTokens, passwordEncoder, analytics, Clock.systemUTC());
    }

    AccountService(
            UserRepository users,
            PetRepository pets,
            CareRecordRepository careRecords,
            CareHistoryRepository history,
            PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwordEncoder,
            AnalyticsService analytics,
            Clock clock) {
        this.users = users;
        this.pets = pets;
        this.careRecords = careRecords;
        this.history = history;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.analytics = analytics;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AccountData exportData(String email) {
        User me = activeUser(email);

        List<Pet> myPets = pets.findAllActiveByOwnerId(me.id());
        List<CareRecord> myCares = careRecords.findByFilters(me.id(), null, null, null, null, null);
        List<CareHistoryEntry> myHistory = history.findByUserIdOrderByOccurredAtDesc(me.id());

        Map<UUID, String> petNames = pets.findAllByIds(myCares.stream().map(CareRecord::petId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Pet::id, Pet::name, (first, second) -> first));

        return new AccountData(
                Instant.now(clock),
                new AccountData.Profile(me.id(), me.fullName(), me.email(), me.createdAt()),
                myPets.stream()
                        .map(pet -> new AccountData.PetData(
                                pet.name(), pet.species().name(), pet.breed(), pet.birthDate()))
                        .toList(),
                myCares.stream()
                        .map(record -> new AccountData.CareData(
                                petNames.getOrDefault(record.petId(), UNKNOWN_PET),
                                record.care(),
                                record.careDate(),
                                record.status().name(),
                                record.userId().equals(me.id()) ? "gestor" : "asignado",
                                record.recurrenceIntervalDays(),
                                record.changeReason()))
                        .toList(),
                myHistory.stream()
                        .map(entry -> new AccountData.HistoryData(
                                entry.petName(),
                                entry.care(),
                                entry.action().name(),
                                entry.previousCareDate(),
                                entry.currentCareDate(),
                                entry.occurredAt(),
                                entry.changeReason()))
                        .toList());
    }

    @Transactional(readOnly = true)
    public DeletionSummary deletionSummary(String email) {
        User me = activeUser(email);
        Plan plan = plan(me);
        return new DeletionSummary(
                plan.pets().size(),
                plan.assignedToMe().size(),
                plan.sharedWithOthers().size(),
                plan.unshared().size(),
                plan.historyEntries());
    }

    @Transactional
    public void deleteAccount(String email) {
        User me = activeUser(email);
        Plan plan = plan(me);
        Instant now = Instant.now(clock);

        // 1. Accesos revocados: los cuidados que le asignaron quedan sin asignar.
        for (CareRecord record : plan.assignedToMe()) {
            careRecords.save(new CareRecord(
                    record.id(), record.userId(), record.petId(), record.care(), record.careDate(),
                    record.createdAt(), record.status(), null, record.recurrenceIntervalDays(),
                    record.originalCareDate(), record.seriesId(), record.changeReason()));
        }

        // 2. Cuidados pendientes que solo ella veía: sin dueño ni asignado no sirven a nadie.
        for (CareRecord record : plan.unshared()) {
            careRecords.deleteById(record.id());
        }

        // 3. Sus mascotas se desactivan (borrado lógico, igual que al eliminar una mascota).
        for (Pet pet : plan.pets()) {
            pets.save(new Pet(pet.id(), pet.ownerId(), pet.name(), pet.species(), pet.breed(),
                    pet.birthDate(), pet.createdAt(), now, now));
        }

        // 4. Ningún enlace de recuperación de contraseña sobrevive a la cuenta.
        resetTokens.deleteByUserId(me.id());

        // 5. La fila se anonimiza: se conservan el id y el nombre (trazabilidad de quién hizo cada
        //    cuidado) y se pierden el correo y la contraseña.
        users.save(new User(
                me.id(),
                me.fullName(),
                "eliminado-" + me.id() + "@eliminado.invalid",
                passwordEncoder.encode(UUID.randomUUID().toString()),
                me.createdAt(),
                now,
                now));

        // El evento lo emite el servidor porque, al terminar, el token de la persona ya no sirve.
        analytics.track(AnalyticsEvent.ACCOUNT_DELETE_SUCCEEDED, Map.of(), null);
    }

    private User activeUser(String email) {
        return users.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .filter(User::isActive)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private Plan plan(User me) {
        List<CareRecord> related = careRecords.findByFilters(me.id(), null, null, null, null, null);

        List<CareRecord> assignedToMe = related.stream()
                .filter(record -> !record.userId().equals(me.id()))
                .filter(record -> me.id().equals(record.assignedToUserId()))
                .filter(record -> isPending(record.status()))
                .toList();

        List<CareRecord> created = related.stream()
                .filter(record -> record.userId().equals(me.id()))
                .filter(record -> isPending(record.status()))
                .toList();

        List<CareRecord> sharedWithOthers = created.stream()
                .filter(record -> record.assignedToUserId() != null && !me.id().equals(record.assignedToUserId()))
                .toList();

        List<CareRecord> unshared = created.stream()
                .filter(record -> record.assignedToUserId() == null || me.id().equals(record.assignedToUserId()))
                .toList();

        return new Plan(
                pets.findAllActiveByOwnerId(me.id()),
                assignedToMe,
                sharedWithOthers,
                unshared,
                history.findByUserIdOrderByOccurredAtDesc(me.id()).size());
    }

    private static boolean isPending(CareRecordStatus status) {
        return status == CareRecordStatus.PENDING || status == CareRecordStatus.RESCHEDULED;
    }

    private record Plan(
            List<Pet> pets,
            List<CareRecord> assignedToMe,
            List<CareRecord> sharedWithOthers,
            List<CareRecord> unshared,
            int historyEntries) { }
}