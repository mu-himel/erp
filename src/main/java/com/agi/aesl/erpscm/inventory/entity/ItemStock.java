package com.agi.aesl.erpscm.inventory.entity;



import com.agi.aesl.erpscm.control_panel.inventory_control.entity.Warehouse;
import com.agi.aesl.erpscm.control_panel.inventory_control.entity.WarehouseStore;
import com.agi.aesl.erpscm.inventory.enums.StockType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
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

    @Column(precision = 38, scale = 4)
    private BigDecimal stockQty;

    @Enumerated(EnumType.STRING)
    private StockType stockType;

    @ManyToOne
    @JsonIgnore
    private Item item;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private WarehouseStore warehouseStore;

    public ItemStock(BigDecimal stockQty, Item item) {
        this.stockQty = stockQty;
        this.item = item;
    }

    public ItemStock(BigDecimal stockQty, Item item, StockType stockType) {
        this.stockQty = stockQty;
        this.item = item;
        this.stockType = stockType;
    }

   public ItemStock(BigDecimal stockQty,  Item item,StockType stockType, Warehouse warehouse) {
       this.stockQty = stockQty;
       this.stockType = stockType;
       this.item = item;
       this.warehouse = warehouse;
   }

   public ItemStock(BigDecimal stockQty, Item item,StockType stockType, Warehouse warehouse, WarehouseStore warehouseStore) {
       this.stockQty = stockQty;
       this.stockType = stockType;
       this.item = item;
       this.warehouse = warehouse;
       this.warehouseStore = warehouseStore;
   }
}
