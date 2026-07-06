<template>
    <div class="page">
        <van-nav-bar title="意见反馈" left-arrow @click-left="$router.back()" fixed />
        <div class="container" style="padding-top:56px;">
            <van-form @submit="onSubmit" ref="formRef">
                <van-cell-group inset>
                    <van-field v-model="form.type" is-link readonly name="type" label="问题类型" placeholder="请选择"
                        @click="showType = true" :rules="[{ required: true, message: '请选择问题类型' }]" />
                    <van-field v-model="form.content" name="content" type="textarea" rows="4" maxlength="300"
                        show-word-limit label="问题描述" placeholder="请详细描述遇到的问题"
                        :rules="[{ required: true, message: '请填写问题描述' }]" />
                    <van-field v-model="form.contact" name="contact" label="联系方式" placeholder="邮箱或手机号（可选）" />
                    <van-uploader v-model="images" :max-count="4" :max-size="5 * 1024 * 1024" result-type="dataUrl"
                        @oversize="onOversize" />
                </van-cell-group>
                <div style="padding:12px 16px;">
                    <van-button type="primary" class="btn-primary" round block native-type="submit">提交反馈</van-button>
                </div>
            </van-form>

            <van-action-sheet v-model:show="showType" :actions="typeActions" cancel-text="取消" @select="onSelectType" />
        </div>
    </div>
</template>

<script>
import { showFailToast, showSuccessToast } from 'vant';
import { ref } from 'vue';

export default {
    name: 'Feedback',
    setup() {
        const formRef = ref(null)
        const form = ref({ type: '', content: '', contact: '' })
        const showType = ref(false)
        const typeActions = ref([
            { name: '功能异常' },
            { name: '体验问题' },
            { name: '新功能建议' },
            { name: '其他' },
        ])
        const images = ref([])
        const onOversize = () => showFailToast('图片不超过 5MB')
        const onSelectType = (a) => { form.value.type = a.name; showType.value = false }
        const onSubmit = () => { showSuccessToast('已提交，感谢反馈！'); history.back() }
        return { formRef, form, showType, typeActions, onSelectType, images, onOversize, onSubmit }
    }
}
</script>
