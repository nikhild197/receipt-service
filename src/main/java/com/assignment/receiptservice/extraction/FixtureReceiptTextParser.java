package com.assignment.receiptservice.extraction;

import com.assignment.receiptservice.domain.LineItem;
import com.assignment.receiptservice.domain.TaxLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class FixtureReceiptTextParser implements ReceiptTextParser {
    private static final Pattern MONEY_AT_END = Pattern.compile("(-?\\d+(?:\\.\\d{2})?)\\s*$");
    private static final Pattern VAT_RATE = Pattern.compile("(?i)VAT\\s+(\\d+(?:\\.\\d+)?)%");

    @Override
    public ReceiptExtraction parse(String rawOcrText) {
        if (rawOcrText == null || rawOcrText.isBlank()) {
            throw new IllegalArgumentException("OCR text must not be blank");
        }

        String merchant = null;
        LocalDate date = null;
        String currency = null;
        BigDecimal grandTotal = null;
        List<TaxLine> taxes = new ArrayList<>();
        List<LineItem> lineItems = new ArrayList<>();

        for (String rawLine : rawOcrText.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith("MERCHANT:")) {
                merchant = valueAfterColon(line);
            } else if (line.startsWith("DATE:")) {
                date = LocalDate.parse(valueAfterColon(line));
            } else if (line.startsWith("CURRENCY:")) {
                currency = valueAfterColon(line).toUpperCase(Locale.ROOT);
            } else if (line.startsWith("TOTAL")) {
                grandTotal = trailingAmount(line);
            } else if (line.toUpperCase(Locale.ROOT).contains("VAT")) {
                taxes.add(parseVat(line));
            } else if (looksLikeLineItem(line)) {
                lineItems.add(parseLineItem(line));
            }
        }

        if (merchant == null || date == null || currency == null || grandTotal == null) {
            throw new IllegalArgumentException("OCR text is missing required receipt fields");
        }

        return new ReceiptExtraction(merchant, date, currency, grandTotal, taxes, lineItems);
    }

    private String valueAfterColon(String line) {
        return line.substring(line.indexOf(':') + 1).trim();
    }

    private boolean looksLikeLineItem(String line) {
        String upper = line.toUpperCase(Locale.ROOT);
        if (line.startsWith("Subtotal")
                || line.startsWith("TOTAL")
                || line.startsWith("(")
                || line.startsWith("MERCHANT:")
                || line.startsWith("DATE:")
                || line.startsWith("CURRENCY:")
                || upper.contains("VAT")) {
            return false;
        }
        return MONEY_AT_END.matcher(line).find();
    }

    private LineItem parseLineItem(String line) {
        Matcher matcher = MONEY_AT_END.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to parse line item: " + line);
        }
        String description = line.substring(0, matcher.start()).trim();
        return new LineItem(description, new BigDecimal(matcher.group(1)));
    }

    private TaxLine parseVat(String line) {
        Matcher rateMatcher = VAT_RATE.matcher(line);
        if (!rateMatcher.find()) {
            throw new IllegalArgumentException("Unable to parse VAT rate: " + line);
        }
        BigDecimal rate = new BigDecimal(rateMatcher.group(1)).movePointLeft(2);
        return new TaxLine("VAT", rate, trailingAmount(line));
    }

    private BigDecimal trailingAmount(String line) {
        Matcher matcher = MONEY_AT_END.matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to parse amount: " + line);
        }
        return new BigDecimal(matcher.group(1));
    }
}
