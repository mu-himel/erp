package com.agi.aesl.erpscm.inventory.entity;



import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

// import javax.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scm_item_stocks")
public class ItemStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDate stockDate;

    private BigDecimal stockQty;

    @Enumerated(EnumType.STRING)
    private StockType stockType;

    @ManyToOne
    @JsonIgnore
    private Item item;



    public ItemStock(BigDecimal stockQty, Item item) {
        this.stockQty = stockQty;
        this.item = item;
    }

    public ItemStock(BigDecimal stockQty, Item item, StockType stockType) {
        this.stockQty = stockQty;
        this.item = item;
        this.stockType = stockType;
    }

//    public ItemStock(BigDecimal stockQty,  Item item,StockType stockType, Warehouse warehouse) {
//        this.stockQty = stockQty;
//        this.stockType = stockType;
//        this.item = item;
//        this.warehouse = warehouse;
//    }
//
//    public ItemStock(BigDecimal stockQty, Item item,StockType stockType, Warehouse warehouse, WarehouseStore warehouseStore) {
//        this.stockQty = stockQty;
//        this.stockType = stockType;
//        this.item = item;
//        this.warehouse = warehouse;
//        this.warehouseStore = warehouseStore;
//    }
}
