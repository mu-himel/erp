package com.agi.aesl.erpscm.price_quotation.entity;

import com.agi.aesl.erpscm.indent.entity.Indent;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStateStatus;
import com.agi.aesl.erpscm.price_quotation.enums.PriceQuotationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "price_quotations")
public class PriceQuotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;
    private String vendorName;
    private String vendorPhoneNo;
    private String vendorEmail;
    private String vendorType;
    private Integer score;
    private Long remoteOfferId;
    private String paymentMethod;
    private Long negotiationHistoryId;
    @ManyToOne
    private Indent rfq;

    @Column(length = 1000)
    private String file;

    @OneToMany(mappedBy = "priceQuotation",cascade = CascadeType.ALL)
    private List<PriceQuotationDetail> quotationDetails;

    @OneToMany(mappedBy = "priceQuotation", cascade = CascadeType.ALL)
    private List<PqTermsAndCondition> termsAndConditions;

    @CreationTimestamp
    private LocalDateTime priceQuotationDate;

    @Enumerated(EnumType.STRING)
    private PriceQuotationStatus priceQuotationStatus;

    @Enumerated(EnumType.STRING)
    private PriceQuotationStateStatus status;

    private Boolean isFinal;

    private String declinedMessage;

    private Boolean isRecommendForCs;

    public PriceQuotation(Long id) {
        this.id = id;
    }
}
