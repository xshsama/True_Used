<template>
    <div class="category-select">
        <van-field :model-value="selectedLabel" :label="label" :placeholder="placeholder" readonly is-link
            @click="open" />
        <van-popup v-model:show="show" class="category-popup" position="bottom" round teleport="body" :z-index="3200"
            :overlay-style="{ zIndex: 3199 }" :style="{ height: '70vh' }">
            <div class="cascader-wrap">
                <template v-if="loading || !options.length">
                    <van-nav-bar title="选择类目" left-text="取消" @click-left="close" />
                    <div class="popup-body">
                        <div v-if="loading" class="loading-wrap">
                            <van-skeleton animated :row="6" />
                        </div>
                        <van-empty v-else description="暂无分类">
                            <van-button size="small" type="primary" @click="load">重试</van-button>
                        </van-empty>
                    </div>
                </template>
                <van-cascader v-else v-model="innerValue" title="选择类目" :options="options"
                    :field-names="{ text: 'text', value: 'value', children: 'children' }" @close="close"
                    @finish="onFinish" />
            </div>
        </van-popup>
    </div>

</template>

<script>
import { listCategories } from '@/api/categories';
import { computed, onMounted, ref, watch } from 'vue';

export default {
    name: 'CategorySelect',
    props: {
        modelValue: { type: [Number, String], default: null },
        label: { type: String, default: '分类' },
        placeholder: { type: String, default: '请选择分类' },
    },
    emits: ['update:modelValue', 'change'],
    setup(props, { emit }) {
        const show = ref(false)
        const options = ref([])
        const innerValue = ref(props.modelValue)
        const pathMap = ref(new Map())
        const loading = ref(false)
        const loaded = ref(false)

        watch(
            () => props.modelValue,
            (v) => {
                innerValue.value = v
            },
        )

        const keyOf = (value) => (value === null || value === undefined ? '' : String(value))
        const selectedLabel = computed(() => pathMap.value.get(keyOf(innerValue.value)) || '')

        const prune = (list) => {
            list.forEach((node) => {
                if (!node.children.length) {
                    delete node.children
                    return
                }
                prune(node.children)
            })
        }

        const fillPathMap = (list, prefix = []) => {
            list.forEach((node) => {
                const path = [...prefix, node.text]
                pathMap.value.set(keyOf(node.value), path.join(' / '))
                if (node.children?.length) {
                    fillPathMap(node.children, path)
                }
            })
        }

        const buildTree = (flat) => {
            if (!Array.isArray(flat)) return []

            const nodes = new Map()
            pathMap.value = new Map()

            flat.forEach((c) => {
                const id = String(c.id)
                const parentId = c.parentId ? String(c.parentId) : null
                nodes.set(id, { text: c.name, value: c.id, parentId, children: [] })
            })

            const roots = []
            nodes.forEach((node) => {
                if (node.parentId && nodes.has(node.parentId)) {
                    nodes.get(node.parentId).children.push(node)
                } else {
                    roots.push(node)
                }
            })

            prune(roots)
            fillPathMap(roots)

            return roots
        }

        const load = async () => {
            loading.value = true
            try {
                const res = await listCategories()
                options.value = buildTree(res)
                loaded.value = true
            } catch (e) {
                console.error(e);
                options.value = [];
            } finally {
                loading.value = false
            }
        }

        const open = () => {
            show.value = true
            if (!loaded.value && !loading.value) {
                load()
            }
        }

        const close = () => {
            show.value = false
        }

        const onFinish = ({ value, selectedOptions }) => {
            const selectedValue = value ?? selectedOptions?.[selectedOptions.length - 1]?.value
            if (selectedValue === null || selectedValue === undefined) return

            innerValue.value = selectedValue
            const label = selectedOptions?.map((option) => option.text).join(' / ')
                || pathMap.value.get(keyOf(selectedValue))
                || ''
            emit('update:modelValue', selectedValue)
            emit('change', { value: selectedValue, label, selectedOptions: selectedOptions || [] })
            show.value = false
        }

        onMounted(load)

        return { show, options, innerValue, selectedLabel, open, close, onFinish, load, loading }
    },
}
</script>

<style scoped>
.cascader-wrap {
    height: 70vh;
    display: flex;
    flex-direction: column;
    background: #fff;
}

.category-popup {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: #fff !important;
}

:deep(.van-cascader) {
    flex: 1;
}

.popup-body {
    flex: 1;
    overflow: auto;
}

.loading-wrap {
    padding: 12px 16px;
}
</style>
