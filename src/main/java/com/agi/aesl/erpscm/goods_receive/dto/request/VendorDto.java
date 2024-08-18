package com.agi.aesl.erpscm.goods_receive.dto.request;

import lombok.Data;

@Data
public class VendorDto {
    private Long id;
    private String name;
    private String vendorPhone;
    private String vendorEmail;
}
