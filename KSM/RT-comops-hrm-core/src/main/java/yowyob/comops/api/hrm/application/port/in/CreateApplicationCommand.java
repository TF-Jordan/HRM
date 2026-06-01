package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record CreateApplicationCommand(
        UUID jobOfferId,
        String candidatNom,
        String candidatPrenom,
        String candidatEmail,
        String candidatTelephone,
        UUID cvFileId,
        UUID lettreMotivationFileId) {
}
