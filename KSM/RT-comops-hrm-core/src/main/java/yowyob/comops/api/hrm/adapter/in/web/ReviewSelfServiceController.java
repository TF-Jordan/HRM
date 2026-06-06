package yowyob.comops.api.hrm.adapter.in.web;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.adapter.in.web.ReviewController.ReviewResponse;
import yowyob.comops.api.hrm.application.port.in.ManageReviewUseCase;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;

/**
 * Review self-service: a worker acknowledges their OWN performance review. Guarded only by a valid
 * user context — the operation is scoped to the employee record linked to the caller's actor, so a
 * worker can never acknowledge another employee's review.
 */
@Profile("!test-memory")
@RestController("hrmReviewSelfServiceController")
@RequestMapping("/api/v1/hrm/reviews/me")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class ReviewSelfServiceController {

    private final ManageReviewUseCase manageReviewUseCase;

    public ReviewSelfServiceController(ManageReviewUseCase manageReviewUseCase) {
        this.manageReviewUseCase = manageReviewUseCase;
    }

    @PutMapping("/{reviewId}/acknowledge")
    public Mono<ResponseEntity<ApiResponse<ReviewResponse>>> acknowledgeMyReview(
            @PathVariable UUID reviewId) {
        return manageReviewUseCase.acknowledgeMyReview(reviewId)
                .map(ReviewResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Review acknowledged.")));
    }

    @RestControllerAdvice(assignableTypes = ReviewSelfServiceController.class)
    static class SelfServiceExceptionHandler {

        @ExceptionHandler(EmployeeNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(ex.getMessage(), "EMPLOYEE_NOT_FOUND"));
        }
    }
}
