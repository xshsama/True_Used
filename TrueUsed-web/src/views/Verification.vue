<template>
    <div class="page">
        <van-nav-bar title="实名认证" left-arrow @click-left="$router.back()" fixed />
        <div class="container" style="padding-top:56px;">
            <div v-if="status === 'approved'" class="result">
                <van-empty image="success" description="已通过实名审核" />
                <div class="tips">姓名：{{ saved?.name }} · 证件号：{{ maskId(saved?.idNo) }}</div>
            </div>
            <div v-else-if="status === 'pending'" class="result">
                <van-empty image="clock" description="认证审核中，请耐心等待" />
            </div>
            <div v-else>
                <van-form @submit="onSubmit" ref="formRef">
                    <van-cell-group inset>
                        <van-field v-model="form.name" name="name" label="姓名" placeholder="请输入真实姓名"
                            :rules="[{ required: true, message: '请输入真实姓名' }]" />
                        <van-field v-model="form.idNo" name="idNo" label="证件号" placeholder="18位身份证号"
                            :rules="[{ validator: validateId, message: '请输入有效证件号' }]" />
                        <div class="uploader-row">
                            <div class="label">身份证正面</div>
                            <van-uploader v-model="frontList" :max-count="1" :max-size="5 * 1024 * 1024"
                                result-type="dataUrl" @oversize="onOversize" />
                        </div>
                        <div class="uploader-row">
                            <div class="label">身份证反面</div>
                            <van-uploader v-model="backList" :max-count="1" :max-size="5 * 1024 * 1024"
                                result-type="dataUrl" @oversize="onOversize" />
                        </div>
                    </van-cell-group>
                    <div style="padding:12px 16px;">
                        <van-button type="primary" class="btn-primary" block round
                            native-type="submit">提交认证</van-button>
                    </div>
                </van-form>
            </div>
        </div>
    </div>
</template>

<script>
import { showFailToast, showSuccessToast, showToast } from 'vant';
import { onMounted, ref } from 'vue';

const KEY = 'tu.verification'

export default {
    name: 'Verification',
    setup() {
        const formRef = ref(null)
        const form = ref({ name: '', idNo: '' })
        const frontList = ref([])
        const backList = ref([])
        const status = ref('none') // none | pending | approved
        const saved = ref(null)

        const validateId = (val) => /^[0-9]{17}[0-9Xx]$/.test(val)
        const onOversize = () => showFailToast('图片不超过 5MB')
        const maskId = (v) => v ? `${v.slice(0, 3)}******${v.slice(-4)}` : ''

        const load = () => {
            try { const s = JSON.parse(localStorage.getItem(KEY) || 'null'); if (s) { status.value = s.status; saved.value = s.data } } catch { }
        }
        onMounted(load)

        const onSubmit = () => {
            if (frontList.value.length === 0 || backList.value.length === 0) { showToast('请上传身份证正反面'); return }
            localStorage.setItem(KEY, JSON.stringify({ status: 'pending', data: { ...form.value } }))
            status.value = 'pending'
            showSuccessToast('已提交审核')
        }

        return { formRef, form, frontList, backList, onOversize, onSubmit, status, saved, maskId, validateId }
    }
}
</script>

<style scoped>
.uploader-row {
    padding: 8px 16px;
}

.uploader-row .label {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 6px;
}

.result .tips {
    text-align: center;
    color: #475569;
    margin-top: 8px;
}
</style>
