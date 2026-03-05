package com.groupproject.ecommerce.service;

import com.groupproject.ecommerce.dto.*;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.OrderItem;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.repository.OrderItemRepository;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.repository.ProductRepository;
import com.groupproject.ecommerce.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // Giữ đúng logic hiện tại: doanh thu chỉ tính COMPLETED
    private final List<OrderStatus> revenueStatuses = List.of(OrderStatus.COMPLETED);

    // =========================
    // Helpers: build time range
    // =========================
    private static class PeriodRange {
        final LocalDateTime start;
        final LocalDateTime endExclusive;
        final boolean filtered;
        final boolean monthly; // true: month/year, false: year-only

        PeriodRange(LocalDateTime start, LocalDateTime endExclusive, boolean filtered, boolean monthly) {
            this.start = start;
            this.endExclusive = endExclusive;
            this.filtered = filtered;
            this.monthly = monthly;
        }
    }

    private PeriodRange resolvePeriod(Integer year, Integer month) {
        if (year == null) {
            return new PeriodRange(null, null, false, false);
        }
        if (month != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
            return new PeriodRange(start, end, true, true);
        } else {
            LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
            LocalDateTime end = LocalDate.of(year + 1, 1, 1).atStartOfDay();
            return new PeriodRange(start, end, true, false);
        }
    }

    // =========================
    // API methods (NEW signatures)
    // =========================

    public DashboardStatsDto getDashboardStats(Integer year, Integer month) {
        PeriodRange pr = resolvePeriod(year, month);

        long totalOrders;
        BigDecimal totalRevenue;

        long awaitingPayment;
        long processing;
        long shipping;
        long completed;
        long cancelled;

        if (!pr.filtered) {
            // all-time như cũ
            totalOrders = orderRepository.count();
            totalRevenue = orderRepository.getTotalRevenueByStatus(OrderStatus.COMPLETED);

            awaitingPayment = Optional.ofNullable(orderRepository.countByStatus(OrderStatus.AWAITING_PAYMENT)).orElse(0L);
            processing = Optional.ofNullable(orderRepository.countByStatus(OrderStatus.PROCESSING)).orElse(0L);
            shipping = Optional.ofNullable(orderRepository.countByStatus(OrderStatus.SHIPPING)).orElse(0L);
            completed = Optional.ofNullable(orderRepository.countByStatus(OrderStatus.COMPLETED)).orElse(0L);
            cancelled = Optional.ofNullable(orderRepository.countByStatus(OrderStatus.CANCELLED)).orElse(0L);
        } else {
            // theo kỳ
            totalOrders =
                    Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.AWAITING_PAYMENT, pr.start, pr.endExclusive)).orElse(0L)
                            + Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.PROCESSING, pr.start, pr.endExclusive)).orElse(0L)
                            + Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.SHIPPING, pr.start, pr.endExclusive)).orElse(0L)
                            + Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.SHIPPED, pr.start, pr.endExclusive)).orElse(0L)
                            + Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.COMPLETED, pr.start, pr.endExclusive)).orElse(0L)
                            + Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.CANCELLED, pr.start, pr.endExclusive)).orElse(0L);

            totalRevenue = orderRepository.sumTotalByStatusesAndPlacedAtBetween(revenueStatuses, pr.start, pr.endExclusive);

            awaitingPayment = Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.AWAITING_PAYMENT, pr.start, pr.endExclusive)).orElse(0L);
            processing = Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.PROCESSING, pr.start, pr.endExclusive)).orElse(0L);
            shipping = Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.SHIPPING, pr.start, pr.endExclusive)).orElse(0L);
            completed = Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.COMPLETED, pr.start, pr.endExclusive)).orElse(0L);
            cancelled = Optional.ofNullable(orderRepository.countByStatusAndPlacedAtBetween(OrderStatus.CANCELLED, pr.start, pr.endExclusive)).orElse(0L);
        }

        // 2 cái này thường là snapshot hiện tại (không filter)
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalOrders(totalOrders);
        dto.setTotalRevenue(totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        dto.setTotalCustomers(totalCustomers);
        dto.setTotalProducts(totalProducts);

        dto.setAwaitingPaymentOrders(awaitingPayment);
        dto.setProcessingOrders(processing);
        dto.setShippingOrders(shipping);
        dto.setCompletedOrders(completed);
        dto.setCancelledOrders(cancelled);

        return dto;
    }

    /**
     * Revenue:
     * - Nếu có year/month: trả theo kỳ
     *   + month: group theo ngày
     *   + year: group theo tháng (date = YYYY-MM-01 để FE parse)
     * - Nếu không có year: fallback days (như cũ)
     */
    public List<RevenueByDateDto> getRevenue(Integer year, Integer month, int daysFallback) {
        PeriodRange pr = resolvePeriod(year, month);

        if (!pr.filtered) {
            // fallback 30 ngày như cũ
            LocalDateTime start = LocalDateTime.now().minusDays(daysFallback);
            List<Order> orders = orderRepository.findOrdersAfterDate(start).stream()
                    .filter(o -> revenueStatuses.contains(o.getStatus()))
                    .toList();

            return groupOrdersByDay(orders);
        }

        List<Order> ordersInRange = orderRepository.findByStatusesAndPlacedAtBetween(revenueStatuses, pr.start, pr.endExclusive);

        if (pr.monthly) {
            return groupOrdersByDay(ordersInRange);
        }
        return groupOrdersByMonth(ordersInRange);
    }

    public List<TopProductDto> getTopProducts(int limit, Integer year, Integer month) {
        PeriodRange pr = resolvePeriod(year, month);

        List<OrderItem> items = (!pr.filtered)
                ? orderItemRepository.findAllCompletedOrderItems()
                : orderItemRepository.findOrderItemsByStatusesAndPlacedAtBetween(revenueStatuses, pr.start, pr.endExclusive);

        Map<Long, TopProductDto> map = new HashMap<>();

        for (OrderItem oi : items) {
            if (oi.getProduct() == null) continue;

            Long pid = oi.getProduct().getProductId();
            TopProductDto cur = map.get(pid);
            if (cur == null) {
                cur = new TopProductDto(
                        pid,
                        oi.getProduct().getName(),
                        0L,
                        BigDecimal.ZERO
                );
                map.put(pid, cur);
            }

            long qty = oi.getQuantity() == null ? 0 : oi.getQuantity();
            BigDecimal lineTotal = oi.getLineTotal() == null ? BigDecimal.ZERO : oi.getLineTotal();

            cur.setTotalSold(cur.getTotalSold() + qty);
            cur.setTotalRevenue(cur.getTotalRevenue().add(lineTotal));
        }

        return map.values().stream()
                .sorted(Comparator.comparing(TopProductDto::getTotalSold).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TopCategoryDto> getTopCategories(int limit, Integer year, Integer month) {
        PeriodRange pr = resolvePeriod(year, month);

        List<OrderItem> items = (!pr.filtered)
                ? orderItemRepository.findAllCompletedOrderItems()
                : orderItemRepository.findOrderItemsByStatusesAndPlacedAtBetween(revenueStatuses, pr.start, pr.endExclusive);

        Map<Long, TopCategoryDto> map = new HashMap<>();

        for (OrderItem oi : items) {
            if (oi.getProduct() == null || oi.getProduct().getCategory() == null) continue;

            Long cid = oi.getProduct().getCategory().getCategoryId();
            TopCategoryDto cur = map.get(cid);
            if (cur == null) {
                cur = new TopCategoryDto(
                        cid,
                        oi.getProduct().getCategory().getName(),
                        0L,
                        BigDecimal.ZERO
                );
                map.put(cid, cur);
            }

            long qty = oi.getQuantity() == null ? 0 : oi.getQuantity();
            BigDecimal lineTotal = oi.getLineTotal() == null ? BigDecimal.ZERO : oi.getLineTotal();

            cur.setTotalSold(cur.getTotalSold() + qty);
            cur.setTotalRevenue(cur.getTotalRevenue().add(lineTotal));
        }

        return map.values().stream()
                .sorted(Comparator.comparing(TopCategoryDto::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // =========================
    // Export PDF
    // =========================

    public byte[] exportDashboardPdf(Integer year, Integer month) {
        // Dùng GLOBAL filter => lấy tất cả theo year/month
        DashboardStatsDto stats = getDashboardStats(year, month);
        List<RevenueByDateDto> revenue = getRevenue(year, month, 30);
        List<TopProductDto> topProducts = getTopProducts(10, year, month);
        List<TopCategoryDto> topCategories = getTopCategories(5, year, month);

        String periodText = buildPeriodText(year, month);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 42, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font hFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph("Dashboard Report", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            doc.add(title);

            Paragraph period = new Paragraph("Kỳ báo cáo: " + periodText, normal);
            period.setSpacingAfter(12);
            doc.add(period);

            // KPI
            Paragraph h1 = new Paragraph("Tóm Tắt KPI", hFont);
            h1.setSpacingAfter(8);          // tăng khoảng cách dưới tiêu đề
            doc.add(h1);
            doc.add(buildKpiTable(stats, normal));
            doc.add(spacer(12));

            // Status breakdown
            Paragraph h2 = new Paragraph("Phân tích trạng thái đơn hàng", hFont);
            h2.setSpacingAfter(8);          // tăng khoảng cách dưới tiêu đề
            doc.add(h2);
            doc.add(buildStatusTable(stats, normal));
            doc.add(spacer(12));

            // Revenue
            Paragraph h3 = new Paragraph("Tóm tắt doanh thu", hFont);
            h3.setSpacingAfter(8);          // tăng khoảng cách dưới tiêu đề
            doc.add(h3);
            doc.add(buildRevenueTable(revenue, year, month, normal));
            doc.add(spacer(12));

            // Top Products
            Paragraph h4 = new Paragraph("Top 10 sản phẩm bán chạy", hFont);
            h4.setSpacingAfter(8);          // tăng khoảng cách dưới tiêu đề
            doc.add(h4);
            doc.add(buildTopProductsTable(topProducts, normal));
            doc.add(spacer(12));

            // Top Categories
            Paragraph h5 = new Paragraph("Top 5 doanh mục bán chạy", hFont);
            h5.setSpacingAfter(8);          // tăng khoảng cách dưới tiêu đề
            doc.add(h5);
            doc.add(buildTopCategoriesTable(topCategories, normal));
            doc.add(spacer(8));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // Nếu lỗi PDF, trả file rỗng để tránh crash (hoặc bạn có thể throw runtime)
            return new byte[0];
        }
    }

    private String buildPeriodText(Integer year, Integer month) {
        if (year == null) return "Tổng tất cả trong thời gian qua";
        if (month != null) return String.format("Tháng %02d/%d", month, year);
        return "Năm " + year;
    }

    private Element spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    // =========================
    // Grouping revenue
    // =========================
    private List<RevenueByDateDto> groupOrdersByDay(List<Order> orders) {
        Map<LocalDate, List<Order>> byDate = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getPlacedAt().toLocalDate(), TreeMap::new, Collectors.toList()));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<RevenueByDateDto> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Order>> e : byDate.entrySet()) {
            BigDecimal sum = e.getValue().stream()
                    .map(o -> o.getTotal() == null ? BigDecimal.ZERO : o.getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new RevenueByDateDto(
                    e.getKey().format(fmt),
                    sum,
                    (long) e.getValue().size()
            ));
        }
        return result;
    }

    private List<RevenueByDateDto> groupOrdersByMonth(List<Order> orders) {
        Map<YearMonth, List<Order>> byMonth = orders.stream()
                .collect(Collectors.groupingBy(o -> YearMonth.from(o.getPlacedAt()), TreeMap::new, Collectors.toList()));

        // date = YYYY-MM-01 để FE parse được Date()
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        List<RevenueByDateDto> result = new ArrayList<>();
        for (Map.Entry<YearMonth, List<Order>> e : byMonth.entrySet()) {
            BigDecimal sum = e.getValue().stream()
                    .map(o -> o.getTotal() == null ? BigDecimal.ZERO : o.getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String dateKey = e.getKey().format(fmt) + "-01";
            result.add(new RevenueByDateDto(
                    dateKey,
                    sum,
                    (long) e.getValue().size()
            ));
        }
        return result;
    }

    // =========================
    // PDF tables
    // =========================
    private PdfPTable buildKpiTable(DashboardStatsDto stats, Font font) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{35, 65});

        addRow(t, "Tổng đơn hàng", String.valueOf(stats.getTotalOrders()), font);
        addRow(t, "Tổng doanh thu", formatVnd(stats.getTotalRevenue()), font);
        addRow(t, "Tổng khách hàng", String.valueOf(stats.getTotalCustomers()), font);
        addRow(t, "Tổng sản phẩm", String.valueOf(stats.getTotalProducts()), font);
        return t;
    }

    private PdfPTable buildStatusTable(DashboardStatsDto stats, Font font) {
        long total = safe(stats.getAwaitingPaymentOrders())
                + safe(stats.getProcessingOrders())
                + safe(stats.getShippingOrders())
                + safe(stats.getCompletedOrders())
                + safe(stats.getCancelledOrders());

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{55, 25, 20});

        addHeader(t, "Trạng thái", font);
        addHeader(t, "Số lượng", font);
        addHeader(t, "Tỷ trọng", font);

        addStatusRow(t, "Chờ thanh toán", stats.getAwaitingPaymentOrders(), total, font);
        addStatusRow(t, "Đang xử lý", stats.getProcessingOrders(), total, font);
        addStatusRow(t, "Đang giao", stats.getShippingOrders(), total, font);
        addStatusRow(t, "Hoàn thành", stats.getCompletedOrders(), total, font);
        addStatusRow(t, "Đã hủy", stats.getCancelledOrders(), total, font);

        return t;
    }

    private PdfPTable buildRevenueTable(List<RevenueByDateDto> revenue, Integer year, Integer month, Font font) {
        boolean monthly = (year != null && month != null);

        PdfPTable t = new PdfPTable(3);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{30, 35, 35});

        addHeader(t, monthly ? "Ngày" : "Tháng", font);
        addHeader(t, "Số đơn", font);
        addHeader(t, "Doanh thu", font);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;

        for (RevenueByDateDto r : revenue) {
            String label = monthly ? r.getDate() : r.getDate().substring(0, 7); // YYYY-MM
            addCell(t, label, font);
            addCellRight(t, String.valueOf(r.getOrderCount()), font);
            addCellRight(t, formatVnd(r.getRevenue()), font);

            totalRevenue = totalRevenue.add(r.getRevenue() == null ? BigDecimal.ZERO : r.getRevenue());
            totalOrders += (r.getOrderCount() == null ? 0 : r.getOrderCount());
        }

        // Total row
        PdfPCell c1 = new PdfPCell(new Phrase("TỔNG", font));
        c1.setPadding(6);
        c1.setBackgroundColor(new Color(240, 240, 240));
        t.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(totalOrders), font));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(6);
        c2.setBackgroundColor(new Color(240, 240, 240));
        t.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(formatVnd(totalRevenue), font));
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c3.setPadding(6);
        c3.setBackgroundColor(new Color(240, 240, 240));
        t.addCell(c3);

        return t;
    }

    private PdfPTable buildTopProductsTable(List<TopProductDto> list, Font font) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{8, 52, 20, 20});

        addHeader(t, "#", font);
        addHeader(t, "Tên sách", font);
        addHeader(t, "Số lượng bán", font);
        addHeader(t, "Doanh thu", font);

        int i = 1;
        for (TopProductDto p : list) {
            addCell(t, String.valueOf(i++), font);
            addCell(t, safeStr(p.getProductName()), font);
            addCellRight(t, String.valueOf(safe(p.getTotalSold())), font);
            addCellRight(t, formatVnd(p.getTotalRevenue()), font);
        }
        if (list.isEmpty()) {
            addCellSpan(t, "Không có dữ liệu", 4, font);
        }
        return t;
    }

    private PdfPTable buildTopCategoriesTable(List<TopCategoryDto> list, Font font) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{8, 52, 20, 20});

        addHeader(t, "#", font);
        addHeader(t, "Danh mục", font);
        addHeader(t, "Số lượng bán", font);
        addHeader(t, "Doanh thu", font);

        int i = 1;
        for (TopCategoryDto c : list) {
            addCell(t, String.valueOf(i++), font);
            addCell(t, safeStr(c.getCategoryName()), font);
            addCellRight(t, String.valueOf(safe(c.getTotalSold())), font);
            addCellRight(t, formatVnd(c.getTotalRevenue()), font);
        }
        if (list.isEmpty()) {
            addCellSpan(t, "Không có dữ liệu", 4, font);
        }
        return t;
    }

    // ===== PDF cell helpers =====
    private void addRow(PdfPTable t, String k, String v, Font f) {
        addCell(t, k, f);
        addCellRight(t, v, f);
    }

    private void addHeader(PdfPTable t, String text, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(6);
        cell.setBackgroundColor(new Color(230, 230, 230));
        t.addCell(cell);
    }

    private void addCell(PdfPTable t, String text, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(6);
        t.addCell(cell);
    }

    private void addCellRight(PdfPTable t, String text, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(cell);
    }

    private void addCellSpan(PdfPTable t, String text, int span, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(10);
        cell.setColspan(span);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        t.addCell(cell);
    }

    private void addStatusRow(PdfPTable t, String name, Long value, long total, Font f) {
        long v = safe(value);
        String pct = (total <= 0) ? "0.0%" : (BigDecimal.valueOf(v)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)) + "%";

        addCell(t, name, f);
        addCellRight(t, String.valueOf(v), f);
        addCellRight(t, pct, f);
    }

    private long safe(Long v) { return v == null ? 0L : v; }
    private String safeStr(String s) { return s == null ? "" : s; }

    private String formatVnd(BigDecimal v) {
        if (v == null) return "0 đ";
        // format đơn giản (bạn có thể thay bằng NumberFormat vi-VN nếu muốn)
        return v.setScale(0, RoundingMode.HALF_UP).toPlainString() + " đ";
    }
}