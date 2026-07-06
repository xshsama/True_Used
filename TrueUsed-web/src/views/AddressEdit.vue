<template>
    <div class="page">
        <van-nav-bar :title="isEdit ? '编辑地址' : '新增地址'" left-arrow @click-left="$router.back()" fixed />
        <div class="container" style="padding-top: 56px;">
            <van-address-edit :address-info="addressInfo" :area-list="areaList" show-postal :show-delete="isEdit"
                show-set-default show-search-result :search-result="searchResult"
                :area-columns-placeholder="['请选择', '请选择', '请选择']" @save="onSave" @delete="onDelete"
                @change-detail="onChangeDetail" />
        </div>
    </div>
</template>

<script>
import { createAddress, deleteAddress, getAddressById, updateAddress } from '@/api/address';
import { areaList } from '@vant/area-data';
import { showFailToast, showSuccessToast } from 'vant';
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export default {
    name: 'AddressEdit',
    setup() {
        const router = useRouter();
        const route = useRoute();
        const isEdit = ref(false);
        const searchResult = ref([]);
        const addressInfo = ref({});

        const onSave = async (content) => {
            try {
                const addressData = {
                    recipientName: content.name,
                    phone: content.tel,
                    province: content.province,
                    city: content.city,
                    district: content.county,
                    detailedAddress: content.addressDetail,
                    isDefault: content.isDefault,
                    areaCode: content.areaCode,
                };
                if (isEdit.value) {
                    await updateAddress(route.params.id, addressData);
                } else {
                    await createAddress(addressData);
                }
                showSuccessToast('保存成功');
                router.back();
            } catch (error) {
                console.error('Address save error:', error);
                let msg = '保存失败';
                // 优先尝试获取后端返回的业务错误信息
                if (error.response?.data) {
                    if (typeof error.response.data === 'object') {
                        msg = error.response.data.message || error.response.data.msg || JSON.stringify(error.response.data);
                    } else {
                        msg = String(error.response.data);
                    }
                } else if (error.message) {
                    msg = error.message;
                }
                showFailToast(msg);
            }
        };

        const onDelete = async () => {
            try {
                await deleteAddress(route.params.id);
                showSuccessToast('删除成功');
                router.back();
            } catch (error) {
                showFailToast('删除失败');
            }
        };

        const onChangeDetail = (val) => {
            if (val) {
                searchResult.value = [{ name: '杭州市', address: '西湖区' }];
            } else {
                searchResult.value = [];
            }
        };

        onMounted(async () => {
            if (route.params.id) {
                isEdit.value = true;
                const address = await getAddressById(route.params.id);
                addressInfo.value = {
                    name: address.recipientName,
                    tel: address.phone,
                    province: address.province,
                    city: address.city,
                    county: address.district,
                    addressDetail: address.detailedAddress,
                    isDefault: address.isDefault,
                    areaCode: address.areaCode || '', // This will be resolved by the component
                };
            } else {
                // For 'add' mode, provide a minimal object to avoid issues
                addressInfo.value = {
                    name: '',
                    tel: '',
                    province: '',
                    city: '',
                    county: '',
                    addressDetail: '',
                    isDefault: false,
                    areaCode: '',
                };
            }
        });

        return {
            isEdit,
            onSave,
            onDelete,
            areaList,
            searchResult,
            onChangeDetail,
            addressInfo
        };
    },
};
</script>

<style scoped>
/* 输入框聚焦样式 */
:deep(.van-cell:focus-within) {
    position: relative;
    z-index: 1;
}

:deep(.van-cell:focus-within::after) {
    content: '';
    position: absolute;
    bottom: 0;
    left: 16px;
    right: 16px;
    height: 1px;
    background-color: var(--primary-color);
    transform: scaleY(0.5);
}

/* 按钮颜色覆盖 */
:deep(.van-button--danger) {
    background-color: var(--primary-color);
    border-color: var(--primary-color);
}

/* 页面布局调整 */
.page {
    min-height: 100vh;
    background: var(--bg-page);
}

.container {
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    padding-top: 56px !important;
    box-sizing: border-box;
}
</style>