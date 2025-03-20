package com.agi.aesl.erpscm.price_quotation.repository;

public class PqQuery {
    private PqQuery(){}
    public static final String PREV_PRICE_QUOTATION_BY_VENDOR_ID = """
        SELECT 
        MAX(pq.id)                 as id,
        vendor_id                  as vendorId,
        vendor_name                as vendorName,  
        payment_method             as paymentMethod, 
        pq.score as score,
        (SELECT sum(pqd2.rfq_qty)  from price_quotation_details pqd2  where pqd2.price_quotation_id  = MAX(pq.id)) as rfqQty,  
        (SELECT sub_total_price from price_quotation_summary pqs where pqs.price_quotation_id = MAX(pq.id)) as totalPrice, 
        (SELECT pq2.price_quotation_status  from price_quotations pq2 where id = MAX(pq.id))  as priceQuotationStatus,
        (SELECT pq3.status  from price_quotations pq3 where id = MAX(pq.id))  as status
    FROM price_quotations pq  
    LEFT JOIN price_quotation_details pqd ON pqd.price_quotation_id  = pq.id
    WHERE pq.vendor_id = :vendorId 
    ORDER BY id DESC LIMIT 1
            """;

    public static final String GET_PRICE_QUOTATION_BY_INDENT_ID = """
            SELECT 
            MAX(pq.id)                 as id,
            vendor_id                  as vendorId,
            vendor_name                as vendorName,  
            payment_method             as paymentMethod, 
            pq.score as score,
            (SELECT sum(pqd2.rfq_qty)  from price_quotation_details pqd2  where pqd2.price_quotation_id  = MAX(pq.id)) as rfqQty,  
            (SELECT sub_total_price from price_quotation_summary pqs where pqs.price_quotation_id = MAX(pq.id)) as totalPrice, 
            (SELECT pq2.price_quotation_status  from price_quotations pq2 where id = MAX(pq.id))  as priceQuotationStatus,
            (SELECT pq3.status  from price_quotations pq3 where id = MAX(pq.id))  as status
        FROM price_quotations pq  
        LEFT JOIN price_quotation_details pqd ON pqd.price_quotation_id  = pq.id
         WHERE pq.rfq_id = :indentId
        GROUP BY vendor_id
            """;
}
