<template>
    <div class="image-uploader" :class="{ 'is-uploading': isUploading }" @dragover.prevent @drop.prevent="handleDrop">
        <div class="image-preview-list">
            <div v-for="(image, index) in previewImages" :key="index" class="image-preview image-tile"
                :style="tileStyle">
                <img :src="image.url" :alt="image.file?.name || 'image'" />
                <button v-if="!readonly" @click="removeImage(index)" class="remove-btn"
                    aria-label="移除图片">&times;</button>
            </div>
            <div class="upload-placeholder image-tile" :class="{ round }" :style="tileStyle" @click="triggerFileInput"
                v-if="canUploadMore && !readonly">
                <span class="plus-icon">+</span>
                <span class="upload-text">{{ hint }}</span>
                <div class="count-badge" v-if="showCount">{{ previewImages.length }}/{{ maxImages }}</div>
                <div v-if="isUploading" class="upload-overlay"><span class="spinner"></span> 上传中</div>
            </div>
        </div>
        <input type="file" ref="fileInput" @change="handleFileChange" :multiple="maxImages > 1" accept="image/*"
            style="display: none" :disabled="readonly" />
        <div v-if="error" class="upload-error">{{ error }}</div>
    </div>

</template>

<script setup>
import { getCloudinarySignature } from '@/api/cloudinary';
import axios from 'axios';
import { computed, onMounted, ref, watch } from 'vue';

const props = defineProps({
    maxImages: {
        type: Number,
        default: 9,
    },
    modelValue: {
        type: Array,
        default: () => [],
    },
    size: {
        type: Number,
        default: 120,
    },
    round: {
        type: Boolean,
        default: false,
    },
    hint: {
        type: String,
        default: '添加图片',
    },
    showCount: {
        type: Boolean,
        default: true,
    },
    readonly: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:modelValue']);

const previewImages = ref([]);
const isUploading = ref(false);
const error = ref(null);
const fileInput = ref(null);

const canUploadMore = computed(() => previewImages.value.length < props.maxImages);
const tileStyle = computed(() => ({
    '--tile-size': props.size + 'px',
    '--tile-radius': props.round ? '9999px' : '12px',
}));

const triggerFileInput = () => {
    fileInput.value.click();
};

const processFiles = async (files) => {
    const list = Array.from(files || []);
    if (!list.length) return;
    const filesToUpload = list.slice(0, props.maxImages - previewImages.value.length);
    if (!filesToUpload.length) return;

    isUploading.value = true;
    error.value = null;

    try {
        const rawSignature = await getCloudinarySignature();
        const signatureData = rawSignature && rawSignature.data ? rawSignature.data : rawSignature;
        const apiKey = signatureData?.api_key || signatureData?.apiKey;
        const timestamp = signatureData?.timestamp;
        const signature = signatureData?.signature;
        const uploadPreset = signatureData?.upload_preset || signatureData?.uploadPreset;

        if (!apiKey || !timestamp || !signature) {
            throw new Error('签名数据缺失');
        }

        const uploadPromises = filesToUpload.map((file) => {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('api_key', apiKey);
            formData.append('timestamp', timestamp);
            formData.append('signature', signature);
            if (uploadPreset) {
                formData.append('upload_preset', uploadPreset);
            }
            return axios.post(
                `https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME}/image/upload`,
                formData
            );
        });

        const results = await Promise.all(uploadPromises);
        const uploadedUrls = results.map(res => res.data.secure_url);

        const newImages = filesToUpload.map((file, index) => ({ file, url: uploadedUrls[index] }));
        previewImages.value.push(...newImages);
        emit('update:modelValue', previewImages.value.map(img => img.url));
    } catch (err) {
        console.error('Upload failed:', err);
        error.value = '图片上传失败，请稍后重试。';
    } finally {
        isUploading.value = false;
        if (fileInput.value) fileInput.value.value = '';
    }
};

const handleFileChange = async (event) => {
    await processFiles(event.target.files);
};

const handleDrop = async (event) => {
    await processFiles(event.dataTransfer?.files);
};

const removeImage = (index) => {
    previewImages.value.splice(index, 1);
    const allUrls = previewImages.value.map(img => img.url);
    emit('update:modelValue', allUrls);
};

// 同步外部 v-model 初始值到内部预览
const syncFromModel = (vals) => {
    if (!Array.isArray(vals)) return;
    previewImages.value = vals.filter(Boolean).map((url) => ({ file: null, url }));
};

onMounted(() => syncFromModel(props.modelValue));
watch(() => props.modelValue, (val) => syncFromModel(val));

</script>

<style scoped>
.image-uploader {
    width: 100%;
}

.image-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
}

.image-tile {
    width: var(--tile-size, 120px);
    height: var(--tile-size, 120px);
    border-radius: var(--tile-radius, 12px);
    position: relative;
    overflow: hidden;
    background: var(--bg-input, #f8fafc);
    border: 1px dashed var(--border-color, #cbd5e1);
    transition: border-color .2s ease, box-shadow .2s ease, transform .1s ease;
}

.image-preview {
    border-style: solid;
}

.image-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--text-secondary, #64748b);
    cursor: pointer;
}

.upload-placeholder:hover {
    border-color: var(--primary-light, #93c5fd);
    color: var(--primary-color, #4CAF50);
    box-shadow: 0 6px 18px -10px rgba(59, 130, 246, .45);
}

.upload-placeholder.round {
    border-radius: 9999px;
}

.plus-icon {
    font-size: 28px;
    line-height: 1;
}

.upload-text {
    font-size: 12px;
    margin-top: 6px;
}

.count-badge {
    position: absolute;
    right: 8px;
    top: 8px;
    font-size: 11px;
    color: var(--text-secondary, #475569);
    background: var(--bg-page, #e2e8f0);
    border-radius: 9999px;
    padding: 2px 6px;
}

.remove-btn {
    position: absolute;
    top: 6px;
    right: 6px;
    width: 22px;
    height: 22px;
    background: rgba(0, 0, 0, .55);
    color: #fff;
    border: 0;
    border-radius: 9999px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    line-height: 1;
}

.image-preview:hover .remove-btn {
    background: var(--danger-color, #ef4444);
}

.upload-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, .7);
    color: var(--text-primary, #334155);
    font-size: 12px;
    gap: 8px;
}

.spinner {
    width: 16px;
    height: 16px;
    border-radius: 9999px;
    border: 2px solid var(--primary-light, #93c5fd);
    border-top-color: var(--primary-color, #4CAF50);
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.upload-error {
    margin-top: 10px;
    font-size: 13px;
    color: var(--danger-color, #ef4444);
}
</style>