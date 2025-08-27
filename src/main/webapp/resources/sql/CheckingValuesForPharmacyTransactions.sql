-- Check the latest bill item data for pharmacy transaction verification
-- This query retrieves the most recent bill item with key financial fields
SELECT id, `CREATEDAT`, `BILLITEMFINANCEDETAILS_ID`, `NETRATE`, `RATE`, `QTY`, `NETVALUE`, `GROSSVALUE` FROM billitem order by id desc LIMIT 10;

-- Check the latest bill item finance details for pharmacy transactions
-- This query fetches the most recent 2 records to compare financial calculations
select `ID`,  `CREATEDAT`,  `LINENETRATE`, `LINEGROSSTOTAL`, `GROSSRATE`, `GROSSTOTAL`, `LINEGROSSRATE`, `LINECOST`, `LINECOSTRATE`, `QUANTITY`, `QUANTITYBYUNITS`,
 `LINECOSTRATE`, `VALUEATCOSTRATE`, `VALUEATPURCHASERATE`, `VALUEATRETAILRATE` from billitemfinancedetails order by id desc limit 10;

-- Check the latest pharmaceutical bill item data
-- This query verifies pharmacy-specific item information including rates and stock references
select `CREATEDAT`, `CREATER_ID`, `ID`, `QTY`, `RETAILRATE`, `PURCHASERATE`, `COSTRATE` ,  `PURCHASERATEPACK`, `RETAILRATEPACK`, `COSTRATEPACK`, `PURCHASEVALUE`, `RETAILVALUE`, `COSTVALUE` from pharmaceuticalbillitem order by id desc limit 10;

-- Check the latest bill header information
-- This query retrieves the most recent bill with key totals and transaction type
select `ID` , `DTYPE`, `CREATEDAT`, `BILLTYPEATOMIC`, `BILLTYPE`,  `NETTOTAL`, `TOTAL`, `SALEVALUE` , `DEPARTMENT_ID`, `FROMDEPARTMENT_ID`, `TODEPARTMENT_ID` from Bill order by id desc limit 10;

-- Check the latest bill finance details summary
-- This query fetches bill-level financial totals for verification
select `ID`, `NETTOTAL`, `GROSSTOTAL`, `TOTALCOSTVALUE`, `TOTALPURCHASEVALUE`, `TOTALRETAILSALEVALUE` from billfinancedetails order by id desc limit 10;