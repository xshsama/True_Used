<template>
    <div class="search-shell"
        :class="{
            'search-shell--compact': size === 'compact',
            'search-shell--no-shortcut': !showShortcut,
            'search-shell--no-submit': !showSubmit
        }">
        <div class="search-icon">
            <div class="i-lucide-search text-lg"></div>
        </div>
        <input type="text" :placeholder="placeholder" v-model="searchQuery"
            class="search-input"
            @keyup.enter="handleSearch" />
        <div v-if="showShortcut" class="search-shortcut">ENTER</div>
        <button v-if="showSubmit" type="button" class="search-submit" @click="handleSearch">搜索</button>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
    placeholder: {
        type: String,
        default: '搜“iPhone 15”看看大家卖多少钱...'
    },
    showShortcut: {
        type: Boolean,
        default: true
    },
    showSubmit: {
        type: Boolean,
        default: true
    },
    size: {
        type: String,
        default: 'default'
    },
    modelValue: {
        type: String,
        default: ''
    }
});

const emit = defineEmits(['search', 'update:modelValue']);

const searchQuery = ref(props.modelValue);

watch(() => props.modelValue, (newValue) => {
    searchQuery.value = newValue;
});

watch(searchQuery, (newValue) => {
    emit('update:modelValue', newValue);
});

const handleSearch = () => {
    emit('search', searchQuery.value);
};
</script>

<style scoped>
.search-shell {
    display: grid;
    width: 100%;
    grid-template-columns: auto minmax(0, 1fr) auto auto;
    align-items: center;
    gap: 10px;
    border: 1px solid rgba(203, 213, 225, 0.85);
    background: rgba(255, 255, 255, 0.92);
    border-radius: 9999px;
    padding: 7px 8px 7px 14px;
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.05);
    transition: all 0.2s ease;
}

.search-shell--no-shortcut {
    grid-template-columns: auto minmax(0, 1fr) auto;
}

.search-shell--no-submit {
    grid-template-columns: auto minmax(0, 1fr) auto;
}

.search-shell--no-submit.search-shell--no-shortcut {
    grid-template-columns: auto minmax(0, 1fr);
}

.search-shell--compact {
    gap: 8px;
    padding: 6px 8px 6px 12px;
}

.search-shell:focus-within {
    border-color: rgba(11, 138, 97, 0.35);
    box-shadow: 0 16px 36px rgba(0, 135, 90, 0.08);
}

.search-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #64748b;
}

.search-input {
    min-width: 0;
    width: 100%;
    border: 0;
    background: transparent;
    color: #334155;
    font-size: 15px;
    outline: none;
}

.search-input::placeholder {
    color: #94a3b8;
}

.search-shell--compact .search-input {
    font-size: 14px;
}

.search-shortcut {
    white-space: nowrap;
    border-radius: 9999px;
    background: #f8fafc;
    padding: 7px 12px;
    color: #94a3b8;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.08em;
}

.search-submit {
    display: inline-flex;
    min-width: 102px;
    align-items: center;
    justify-content: center;
    white-space: nowrap;
    border: 0;
    border-radius: 9999px;
    background: linear-gradient(135deg, #00875A, #0f5b45);
    padding: 12px 20px;
    color: #fff;
    font-size: 14px;
    font-weight: 700;
    cursor: pointer;
    line-height: 1;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
    box-shadow: 0 10px 22px rgba(0, 135, 90, 0.2);
}

.search-submit:hover {
    transform: translateY(-1px);
}

.search-shell--compact .search-submit {
    min-width: 88px;
    padding: 10px 16px;
    font-size: 13px;
}
</style>
