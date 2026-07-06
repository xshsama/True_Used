const SELLER_CONDITION_LABELS = {
    NEW: '全新',
    LIKE_NEW: '95新',
    GOOD: '9成新',
    FAIR: '8成新',
    POOR: '战损版'
};

const INSPECTION_GRADE_LABELS = {
    S: 'S级成色',
    A: 'A级成色',
    B: 'B级成色',
    C: 'C级成色',
    X: '未通过验货'
};

const PRODUCT_SALE_STATUS_LABELS = {
    PENDING: '待平台处理',
    ON_SALE: '在售',
    LOCKED: '交易锁定',
    SOLD: '已售',
    OFF_SHELF: '已下架'
};

export function formatSellerClaimCondition(code) {
    return SELLER_CONDITION_LABELS[code] || code || '';
}

export function formatPlatformInspectionGrade(code) {
    return INSPECTION_GRADE_LABELS[code] || (code ? `${code}级成色` : '');
}

export function formatProductSaleStatus(status) {
    return PRODUCT_SALE_STATUS_LABELS[status] || status || '';
}

export function isOfficialInspectionTrade(product = {}) {
    return product.tradeModel === 'OFFICIAL_INSPECTION' || product.isOfficial === true;
}

export function normalizeProductTrade(product = {}) {
    const tradeMode = isOfficialInspectionTrade(product)
        ? 'OFFICIAL_INSPECTION'
        : (product.tradeModel || 'FREE_TRADING');
    const hasPlatformInspection = tradeMode === 'OFFICIAL_INSPECTION';
    const saleStatus = product.status || '';
    const sellerClaimConditionCode = product.sellerClaimCondition || product.condition || '';
    const sellerClaimConditionLabel = formatSellerClaimCondition(sellerClaimConditionCode);
    const platformInspectionGradeCode = product.inspectionGrade || '';
    const platformInspectionGradeLabel = formatPlatformInspectionGrade(platformInspectionGradeCode);

    let inspectionStatus = 'not_applicable';
    if (hasPlatformInspection) {
        if (platformInspectionGradeCode === 'X') {
            inspectionStatus = 'rejected';
        } else if (platformInspectionGradeCode) {
            inspectionStatus = 'passed';
        } else {
            inspectionStatus = 'pending';
        }
    }

    const fulfillmentMode = hasPlatformInspection ? 'PLATFORM_SHIP' : 'SELLER_SHIP';
    const fulfillmentModeLabel = hasPlatformInspection ? '平台发货' : '卖家发货';
    const saleStatusLabel = formatProductSaleStatus(saleStatus);
    const canBuy = saleStatus === 'ON_SALE';

    let buyDisabledReason = '';
    if (!canBuy) {
        if (hasPlatformInspection && inspectionStatus === 'pending') {
            buyDisabledReason = '平台验货中，验货通过后才会上架';
        } else if (hasPlatformInspection && inspectionStatus === 'rejected') {
            buyDisabledReason = '该商品未通过平台验货，当前不可购买';
        } else if (saleStatus === 'LOCKED') {
            buyDisabledReason = '该商品正在交易中，请稍后再看';
        } else if (saleStatus === 'SOLD') {
            buyDisabledReason = '该商品已经售出';
        } else if (saleStatus === 'OFF_SHELF') {
            buyDisabledReason = '该商品已下架';
        } else if (saleStatus === 'PENDING') {
            buyDisabledReason = hasPlatformInspection
                ? '平台正在处理中，暂不可购买'
                : '商品暂未上架';
        } else {
            buyDisabledReason = '该商品当前不可购买';
        }
    }

    return {
        tradeMode,
        tradeModeLabel: hasPlatformInspection ? '平台验货' : '卖家自出',
        hasPlatformInspection,
        saleStatus,
        saleStatusLabel,
        inspectionStatus,
        sellerClaimConditionCode,
        sellerClaimConditionLabel,
        platformInspectionGradeCode,
        platformInspectionGradeLabel,
        fulfillmentMode,
        fulfillmentModeLabel,
        canBuy,
        buyDisabledReason,
        primaryConditionLabel: hasPlatformInspection
            ? (platformInspectionGradeLabel || '待平台验货')
            : (sellerClaimConditionLabel || '成色待补充'),
        secondaryConditionLabel: hasPlatformInspection
            ? (sellerClaimConditionLabel ? `卖家自述 ${sellerClaimConditionLabel}` : '卖家成色待补充')
            : '',
        inspectionFee: hasPlatformInspection ? Number(product.inspectionFee ?? 29) : 0
    };
}
