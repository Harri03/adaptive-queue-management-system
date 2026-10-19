
package com.queue.queuemanagement.controller;

import com.queue.queuemanagement.entity.QueueEntry;
import com.queue.queuemanagement.entity.QueueEntryResponse;
import com.queue.queuemanagement.service.QueueEntryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:5175",
        "http://10.203.20.27:5173",
        "http://10.203.20.27:5174",
        "http://10.203.20.27:5175"
})
@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueEntryService queueEntryService;

    public QueueController(QueueEntryService queueEntryService) {
        this.queueEntryService = queueEntryService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String qrDate) {

        if (customerName == null || customerName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Customer name is required.");
        }

        if (phoneNumber == null
                || !phoneNumber.matches("[6-9][0-9]{9}")) {
            return ResponseEntity.badRequest()
                    .body("Enter a valid 10-digit Indian mobile number.");
        }

        if (qrDate == null || qrDate.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("QR date is required.");
        }

        final LocalDate parsedQrDate;

        try {
            parsedQrDate = LocalDate.parse(qrDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid QR date. Use yyyy-MM-dd format.");
        }

        if (!parsedQrDate.equals(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body("This QR code has expired. Please scan today's QR code.");
        }

        QueueEntry customer = queueEntryService.addCustomer(
                customerName.trim(),
                phoneNumber
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(required = false) String date) {

        try {
            if (date == null || date.trim().isEmpty()) {
                return ResponseEntity.ok(
                        queueEntryService.getAllCustomers()
                );
            }

            LocalDate selectedDate = LocalDate.parse(date);

            List<QueueEntryResponse> customers =
                    queueEntryService.getCustomersByDate(selectedDate);

            return ResponseEntity.ok(customers);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date. Use yyyy-MM-dd format.");
        }
    }

    @PutMapping("/{id}/call")
    public ResponseEntity<?> callCustomer(@PathVariable Long id) {
        QueueEntry customer = queueEntryService.callCustomer(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<?> startService(@PathVariable Long id) {
        QueueEntry customer = queueEntryService.startService(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeCustomer(@PathVariable Long id) {
        QueueEntry customer = queueEntryService.completeCustomer(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }

    @GetMapping("/average-service-time")
    public ResponseEntity<Double> getAverageServiceTime() {
        return ResponseEntity.ok(
                queueEntryService.calculateAverageServiceTime()
        );
    }

    @PutMapping("/{id}/late")
    public ResponseEntity<?> markCustomerLate(@PathVariable Long id) {
        QueueEntry customer = queueEntryService.markCustomerLate(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}/arrive")
    public ResponseEntity<?> arriveLateCustomer(@PathVariable Long id) {
        QueueEntry customer = queueEntryService.arriveLateCustomer(id);

        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customer);
    }


}
