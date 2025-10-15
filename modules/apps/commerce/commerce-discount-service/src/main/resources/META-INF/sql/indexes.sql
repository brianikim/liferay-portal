create index IX_F7FFBCCA on CDiscountCAccountGroupRel (commerceAccountGroupId);
create unique index IX_9D768AF5 on CDiscountCAccountGroupRel (commerceDiscountId, commerceAccountGroupId);

create index IX_3CAC096A on CommerceDiscount (companyId, active_, couponCode[$COLUMN_LENGTH:75$]);
create index IX_E063D0AD on CommerceDiscount (companyId, couponCode[$COLUMN_LENGTH:75$]);
create unique index IX_D294CDB7 on CommerceDiscount (companyId, externalReferenceCode[$COLUMN_LENGTH:75$]);
create index IX_1CCF5211 on CommerceDiscount (companyId, status, active_, levelType[$COLUMN_LENGTH:75$]);
create index IX_4423C101 on CommerceDiscount (status, active_);
create index IX_52CB3DB8 on CommerceDiscount (status, displayDate);
create index IX_DE0C3C39 on CommerceDiscount (status, expirationDate);
create index IX_C89FCCE7 on CommerceDiscount (target[$COLUMN_LENGTH:75$]);
create index IX_F1A4C552 on CommerceDiscount (uuid_[$COLUMN_LENGTH:75$]);

create unique index IX_E082887A on CommerceDiscountAccountRel (commerceAccountId, commerceDiscountId);
create index IX_6EA2AA99 on CommerceDiscountAccountRel (commerceDiscountId);
create index IX_CEE71686 on CommerceDiscountAccountRel (uuid_[$COLUMN_LENGTH:75$]);

create unique index IX_614617A on CommerceDiscountOrderTypeRel (commerceDiscountId, commerceOrderTypeId);
create index IX_707E0345 on CommerceDiscountOrderTypeRel (commerceOrderTypeId);
create index IX_CEE22E81 on CommerceDiscountOrderTypeRel (uuid_[$COLUMN_LENGTH:75$]);

create index IX_4A48FE69 on CommerceDiscountRel (classNameId, classPK, typeSettings[$COLUMN_LENGTH:2000000$]);
create index IX_B243FDED on CommerceDiscountRel (classNameId, commerceDiscountId, classPK);
create index IX_A6E848CE on CommerceDiscountRel (commerceDiscountId);

create index IX_CB9E6769 on CommerceDiscountRule (commerceDiscountId);

create index IX_28CE20FF on CommerceDiscountUsageEntry (commerceDiscountId, commerceAccountId, commerceOrderId);
create index IX_E40C6220 on CommerceDiscountUsageEntry (commerceDiscountId, commerceOrderId);