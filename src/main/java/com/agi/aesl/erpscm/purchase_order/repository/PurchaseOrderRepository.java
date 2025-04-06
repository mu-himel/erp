package com.agi.aesl.erpscm.purchase_order.repository;

import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.employee.entity.Employee;
import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.indent.repository.IndentRepository;
import com.agi.aesl.erpscm.price_quotation.entity.PriceQuotationDetail;
import com.agi.aesl.erpscm.price_quotation.repository.PqQuery;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrder;
import com.agi.aesl.erpscm.purchase_order.entity.PurchaseOrderWarehouseDetail;
import com.agi.aesl.erpscm.purchase_order.enums.PurchaseOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder,Long>, PoQuery {
    @Query(value = getPendingPOs, countQuery = countPendingPOs, nativeQuery = true)
    Page<PendingPOItemDetail> findAllPendingPOs(String vendor,String csNo,
                                                String poNo,Long categoryId,
                                                Long subCategoryId,
                                                LocalDateTime fromDate,
                                                LocalDateTime toDate,
                                                List<String> status,Pageable pageable);

    @Query(value = getPendingVerificationPOs, countQuery = countGetPendingVerificationPOs, nativeQuery = true)
    Page<PendingPOItemDetail> findAllPendingVerificationPOs(String userId,String vendor,
                                        String csNo, String poNo,
                                        Long categoryId, Long subCategoryId,
                                        LocalDateTime fromDate, LocalDateTime toDate,
                                        List<String> status,String searchStatus,Pageable pageable);


    @Query(value = getPendingApprovalPOs, countQuery = countGetPendingApprovalPOs, nativeQuery = true)
    Page<PendingPOItemDetail> findAllPendingApprovalPOs(String userId,String vendor,
                                                        String csNo, String poNo,
                                                        Long categoryId, Long subCategoryId,
                                                        LocalDateTime fromDate, LocalDateTime toDate,
                                                        List<String> status,String searchStatus, Pageable pageable);

    @Query(value = getClosedPOs, countQuery = countClosedPOs, nativeQuery = true)
    Page<PendingPOItemDetail> findAllClosedPOs( String vendor,
            String csNo, String poNo, Long categoryId, Long subCategoryId,
            LocalDateTime fromDate, LocalDateTime toDate,
            List<String> status,
            Pageable pageable);

    @Query(value = getApprovedPOs, countQuery = countApprovedPOs, nativeQuery = true)
    Page<ClosedPOListItem> findAllApprovedPos(
            String vendor,
            String csNo, String poNo, Long categoryId, Long subCategoryId,
            LocalDateTime fromDate, LocalDateTime toDate,
            List<String> status,
            Pageable pageable);

    List<PurchaseOrderDetailInfo> findAllByPoGroupId(Long id);

    @Query(value = purchaseOrderDetail,nativeQuery = true)
    List<PqDetailInfo> getPurchaseOrderDetail(Long poId);

    Long countAllByCsId(Long id);

    interface PendingPOItemDetail{
        Long getId();

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime getPoDate();
        String getPoIds();
        String getIndentNo();
        String getPoNo();
        String getVendorName();
        String getCategoryName();
        BigDecimal getItemQty();
        BigDecimal getTotalOrderQty();
        String getStatus();
    }

    interface PoListItem{
        Long getPoGroupId();
        Long getId();
        String getPoDate();
        String getIndentNo();
        String getCategoryName();
        Long getItemQty();
        Long getTotalOrderQty();
        String getDeliveryDate();
        String getRemainTime();
        String getStatus();
    }

    interface ClosedPOListItem extends PoListItem{
        String getVendorName();
    }

    interface PurchaseOrderDetailInfo {
        Long getId();
        CsInfo getCs();
        String getPoNo();
        LocalDate getCreatedAt();
        LocalDate getPoDate();
        PurchaseOrderStatus getStatus();
        List<POD> getPurchaseOrderDetails();
        Employee getRequestedBy();
        String getDeliveryChargeType();
    }

    interface CsInfo {
        Long getId();
        IndentInfo getIndent();
    }

    interface IndentInfo {
        Long getId();
        String getIndentNo();
        CategoryInfo getCategory();
        CategoryInfo getSubCategory();
        WarehouseInfo getSingleWarehouse();
        WarehouseInfo getWarehouse();
    }

    interface CategoryInfo {
        Long getId();
        String getCode();
        String getName();
    }

    interface POD{
        Long getId();
        String getPoNo();
        LocalDate getDeliveryDate();

        String getItemName();
        BigDecimal getDeliveryQty();
        BigDecimal getDeliveryCharge();
        BigDecimal getVatAmount();
        BigDecimal getVatPercent();
        BigDecimal getTotalPrice();
        BigDecimal getSubTotal();
//        Warehouse getWarehouse();
        List<PurchaseOrderWarehouseDetail> getWarehouseDetailList();
        CsVendorDetailInfo getCsVendorDetail();
    }

    interface CsVendorDetailInfo {

        Long getId();
        BigDecimal getDiscountAmount();
        BigDecimal getOrderQty();
        BigDecimal getTotalPrice();
        BigDecimal getVatAmount();
        String getTransactionType();
        CsDetailInfo getCsDetail();
        Long getVendorId();

        PriceQuotationInfo getPriceQuotation();

    }

    interface PriceQuotationInfo {

        Long getId();
        String getVendorName();
        List<PriceQuotationDetail> getQuotationDetails();
        Long getVendorId();
        String getVendorPhoneNo();
        String getVendorEmail();
        Integer getScore();
        Long getRemoteOfferId();
        String getPaymentMethod();
        Long getNegotiationHistoryId();
    }

    interface CsDetailInfo {
        Long getId();
        IndentDetailInfo getIndentDetail();

    }

    interface IndentDetailInfo {
        String getItemAttribute();
        Long getId();
        IndentInfo getIndent();
    }



    interface PqDetailInfo {
        Long getPoId();
        Long getPodId();
        BigDecimal getVendorPartialVatAmount();
        String getItemName();
        String getTransactionType();
        BigDecimal getTotalPrice();
        BigDecimal getOrderQty();
        BigDecimal getRemainingQty();
        BigDecimal getDeliveryOrderQty();
        LocalDate getDeliveryDate();
        Long getPriceQuotationId();
        Long getWarehouseId();
        String getSummary();
        Boolean getIsAitAdded();
        Boolean getIsVatAdded();
        Long getDeliveryQty();
        BigDecimal getUnitPrice();
        BigDecimal getDeliveryCharge();

        BigDecimal getVatAmount();
        BigDecimal getSubTotal();
    }

    interface WarehouseInfo{
        Long getId();
    }
}
