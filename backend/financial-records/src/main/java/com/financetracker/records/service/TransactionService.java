package com.financetracker.records.service;

import com.financetracker.records.dto.TransactionRequest;
import com.financetracker.records.dto.TransactionResponse;
import com.financetracker.records.model.Transaction;
import com.financetracker.records.model.TransactionType;
import com.financetracker.records.repository.TransactionRepository;
import com.financetracker.records.util.CategoryConstants;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionResponse> getTransactions(String description, String type, String category,
                                                   LocalDate startDate, LocalDate endDate) {
        Specification<Transaction> specification = buildSpecification(description, type, category, startDate, endDate);

        return transactionRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "transactionDate").and(Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id));
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setNotes(request.getNotes());

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id));

        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setNotes(request.getNotes());

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new NoSuchElementException("Transaction not found with id: " + id);
        }
        transactionRepository.deleteById(id);
    }

    public Map<String, Object> getDashboardSummary() {
        List<Transaction> transactions = transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "transactionDate", "id"));

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenditure = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentBalance = totalIncome.subtract(totalExpenditure);

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfCurrentMonth = currentMonth.atDay(1);
        LocalDate endOfCurrentMonth = currentMonth.atEndOfMonth();

        BigDecimal currentMonthIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .filter(t -> !t.getTransactionDate().isBefore(startOfCurrentMonth) && !t.getTransactionDate().isAfter(endOfCurrentMonth))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentMonthExpenditure = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> !t.getTransactionDate().isBefore(startOfCurrentMonth) && !t.getTransactionDate().isAfter(endOfCurrentMonth))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TransactionResponse> recentTransactions = transactions.stream()
                .limit(5)
                .map(this::toResponse)
                .toList();

        List<Map<String, Object>> monthlySeries = buildMonthlyIncomeExpenseSeries(transactions);
        Map<String, BigDecimal> categoryBreakdown = buildCategoryBreakdown(transactions);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenditure", totalExpenditure);
        summary.put("currentBalance", currentBalance);
        summary.put("currentMonthIncome", currentMonthIncome);
        summary.put("currentMonthExpenditure", currentMonthExpenditure);
        summary.put("transactionCount", transactions.size());
        summary.put("monthlyIncomeVsExpense", monthlySeries);
        summary.put("categoryWiseExpenditure", categoryBreakdown);
        summary.put("recentTransactions", recentTransactions);
        return summary;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.addAll(CategoryConstants.EXPENSE_CATEGORIES);
        categories.addAll(CategoryConstants.INCOME_CATEGORIES);
        return categories.stream().distinct().toList();
    }

    private Specification<Transaction> buildSpecification(String description, String type, String category,
                                                        LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (description != null && !description.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }

            if (type != null && !type.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("type"), TransactionType.valueOf(type.trim().toUpperCase())));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<Map<String, Object>> buildMonthlyIncomeExpenseSeries(List<Transaction> transactions) {
        List<Map<String, Object>> monthlySeries = new ArrayList<>();
        List<YearMonth> months = transactions.stream()
                .map(t -> YearMonth.from(t.getTransactionDate()))
                .distinct()
                .sorted()
                .toList();

        if (months.isEmpty()) {
            return monthlySeries;
        }

        for (YearMonth month : months) {
            BigDecimal income = transactions.stream()
                    .filter(t -> YearMonth.from(t.getTransactionDate()).equals(month) && t.getType() == TransactionType.INCOME)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = transactions.stream()
                    .filter(t -> YearMonth.from(t.getTransactionDate()).equals(month) && t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("month", month.toString());
            entry.put("income", income);
            entry.put("expense", expense);
            monthlySeries.add(entry);
        }
        return monthlySeries;
    }

    private Map<String, BigDecimal> buildCategoryBreakdown(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getPaymentMethod(),
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }
}
