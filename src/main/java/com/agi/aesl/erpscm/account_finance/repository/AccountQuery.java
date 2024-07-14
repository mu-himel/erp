package com.agi.aesl.erpscm.account_finance.repository;

public interface AccountQuery {

    String getPendingAccounts = """
            SELECT 
                la.id,
                la.account_no accountNo,
                ws.store_name as store,
                ipc.name as category,
                ic.name as subCategory,
                i.item_attribute_name as product,
                la.group_account as groupAccount
            FROM ledger_accounts la
            LEFT JOIN scm_items i ON i.id = la.item_id
            LEFT JOIN scm_item_categories ic ON ic.id = i.item_category_id
            LEFT JOIN scm_item_categories ipc ON ipc.id = i.item_parent_category_id
            LEFT JOIN scm_category_warehouse_stores cws ON cws.category_id = ic.id
            LEFT JOIN scm_warehouse_stores ws ON ws.id = cws.warehouse_store_id
            """;

    String countPendingAccounts = "SELECT COUNT(*) FROM ("+getPendingAccounts+") total";

    interface PendingAccount{
        Long getId();
        String getAccountNo();
        String getStore();
        String getCategory();
        String getSubCategory();
        String getProduct();
        String getGroupAccount();
    }
}
