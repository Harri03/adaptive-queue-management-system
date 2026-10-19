
package com.queue.queuemanagement.service;

import com.queue.queuemanagement.entity.QueueEntry;
import com.queue.queuemanagement.entity.QueueEntryResponse;
import com.queue.queuemanagement.repository.QueueEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class QueueEntryService {

    private final QueueEntryRepository queueEntryRepository;

    public QueueEntryService(QueueEntryRepository queueEntryRepository) {
        this.queueEntryRepository = queueEntryRepository;
    }

    // Add a new customer with name and phone number
    public QueueEntry addCustomer(String customerName, String phoneNumber) {

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }

        if (phoneNumber == null || !phoneNumber.matches("[6-9][0-9]{9}")) {
            throw new IllegalArgumentException(
                    "Enter a valid 10-digit Indian mobile number"
            );
        }

        // Get today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        // Find the highest token number for today only
        QueueEntry lastEntry =
                queueEntryRepository
                        .findTopByJoinedAtGreaterThanEqualAndJoinedAtLessThanOrderByTokenNumberDesc(
                                startOfDay,
                                startOfNextDay
                        );

        // Generate the next token
        int nextToken = 1;

        if (lastEntry != null) {
            nextToken = lastEntry.getTokenNumber() + 1;
        }

        // Create the customer record
        QueueEntry queueEntry = new QueueEntry();
        queueEntry.setTokenNumber(nextToken);
        queueEntry.setCustomerName(customerName.trim());
        queueEntry.setPhoneNumber(phoneNumber);
        queueEntry.setStatus("WAITING");
        queueEntry.setJoinedAt(LocalDateTime.now());

        // Save the customer
        return queueEntryRepository.save(queueEntry);
    }


    // Get today's customers by default
    public List<QueueEntryResponse> getAllCustomers() {
        System.out.println("Backend date: " + LocalDate.now());
        return getCustomersByDate(LocalDate.now());
    }

    // Get customers who joined on a selected date
    public List<QueueEntryResponse> getCustomersByDate(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<QueueEntry> customers =
                queueEntryRepository
                        .findByJoinedAtGreaterThanEqualAndJoinedAtLessThan(
                                start, end
                        );

        return buildCustomerResponses(customers);
    }

    // Build queue positions and estimated waiting times
    private List<QueueEntryResponse> buildCustomerResponses(
            List<QueueEntry> customers) {

        customers.sort((customer1, customer2) -> {
            int priority1 = getQueuePriority(customer1.getStatus());
            int priority2 = getQueuePriority(customer2.getStatus());

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }

            return Integer.compare(
                    customer1.getTokenNumber(),
                    customer2.getTokenNumber()
            );
        });

        List<QueueEntryResponse> response = new ArrayList<>();

        double averageServiceTimeSeconds =
                calculateAverageServiceTime() * 60;

        QueueEntry servingCustomer = null;

        for (QueueEntry customer : customers) {
            if ("SERVING".equals(customer.getStatus())) {
                servingCustomer = customer;
                break;
            }
        }

        double remainingServiceTime = 0;

        if (servingCustomer != null
                && servingCustomer.getServiceStartTime() != null) {

            long elapsedTime = Math.max(
                    0,
                    Duration.between(
                            servingCustomer.getServiceStartTime(),
                            LocalDateTime.now()
                    ).getSeconds()
            );

            remainingServiceTime = Math.max(
                    0,
                    averageServiceTimeSeconds - elapsedTime
            );
        }

        int position = 0;

        for (QueueEntry customer : customers) {

            String status = customer.getStatus();
            long estimatedWaitingTime;
            int customerPosition;

            if ("COMPLETED".equals(status)) {

                customerPosition = 0;
                estimatedWaitingTime = 0;

            } else if ("SERVING".equals(status)) {

                customerPosition = 1;
                position = 1;
                estimatedWaitingTime = (long) remainingServiceTime;

            } else {

                position++;
                customerPosition = position;

                if (servingCustomer != null) {
                    estimatedWaitingTime = (long) (
                            remainingServiceTime
                                    + (position - 1)
                                    * averageServiceTimeSeconds
                    );
                } else {
                    estimatedWaitingTime = (long) (
                            (position - 1) * averageServiceTimeSeconds
                    );
                }
            }

            estimatedWaitingTime = Math.max(0, estimatedWaitingTime);

            long minutes = estimatedWaitingTime / 60;
            long seconds = estimatedWaitingTime % 60;

            String estimatedWaitingTimeText =
                    minutes + " min " + seconds + " sec";

            QueueEntryResponse customerResponse =
                    new QueueEntryResponse(
                            customer.getId(),
                            customer.getTokenNumber(),
                            customer.getCustomerName(),
                            status,
                            customerPosition,
                            estimatedWaitingTimeText
                    );

            response.add(customerResponse);
        }

        return response;
    }

    // Define the priority of each queue status
    private int getQueuePriority(String status) {

        if ("SERVING".equals(status)) {
            return 1;
        }

        if ("CALLED".equals(status)) {
            return 2;
        }

        if ("WAITING".equals(status)) {
            return 3;
        }

        if ("LATE".equals(status)) {
            return 4;
        }

        if ("COMPLETED".equals(status)) {
            return 5;
        }

        return 6;
    }

    // Calculate average service time in minutes
    public double calculateAverageServiceTime() {

        List<QueueEntry> completedCustomers =
                queueEntryRepository.findByStatus("COMPLETED");

        if (completedCustomers.isEmpty()) {
            return 10;
        }

        long totalDuration = 0;
        int validDurationCount = 0;

        for (QueueEntry customer : completedCustomers) {

            Long duration = customer.getServiceDurationMinutes();

            if (duration != null && duration > 0) {
                totalDuration += duration;
                validDurationCount++;
            }
        }

        if (validDurationCount == 0) {
            return 10;
        }

        return (double) totalDuration / validDurationCount;
    }

    // Call a customer
    public QueueEntry callCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {
            customer.setStatus("CALLED");
            return queueEntryRepository.save(customer);
        }

        return null;
    }

    // Mark a customer as late
    public QueueEntry markCustomerLate(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {
            customer.setStatus("LATE");
            return queueEntryRepository.save(customer);
        }

        return null;
    }

    // Mark a late customer as arrived
    public QueueEntry arriveLateCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            QueueEntry servingCustomer =
                    queueEntryRepository.findFirstByStatus("SERVING");

            if (servingCustomer != null) {
                customer.setStatus("LATE");
                customer.setLateCustomerArrived(true);
            } else {
                customer.setStatus("SERVING");
                customer.setServiceStartTime(LocalDateTime.now());
                customer.setLateCustomerArrived(false);
            }

            return queueEntryRepository.save(customer);
        }

        return null;
    }

    // Start serving a customer
    public QueueEntry startService(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {
            customer.setStatus("SERVING");
            customer.setServiceStartTime(LocalDateTime.now());

            return queueEntryRepository.save(customer);
        }

        return null;
    }

    // Complete a customer's service
    public QueueEntry completeCustomer(Long id) {

        QueueEntry customer =
                queueEntryRepository.findById(id).orElse(null);

        if (customer != null) {

            LocalDateTime endTime = LocalDateTime.now();
            long duration = 1;

            if (customer.getServiceStartTime() != null) {
                duration = Math.max(
                        1,
                        Duration.between(
                                customer.getServiceStartTime(),
                                endTime
                        ).toMinutes()
                );
            }

            customer.setServiceDurationMinutes(duration);
            customer.setStatus("COMPLETED");

            // Promote the first arrived late customer, if available
            List<QueueEntry> lateCustomers =
                    queueEntryRepository.findByStatus("LATE");

            for (QueueEntry lateCustomer : lateCustomers) {

                if (lateCustomer.isLateCustomerArrived()) {
                    lateCustomer.setStatus("SERVING");
                    lateCustomer.setServiceStartTime(LocalDateTime.now());
                    lateCustomer.setLateCustomerArrived(false);

                    queueEntryRepository.save(lateCustomer);
                    break;
                }
            }

            return queueEntryRepository.save(customer);
        }

        return null;
    }
}
