<template>
    <div class="category-select">
        <van-field :model-value="selectedLabel" :label="label" :placeholder="placeholder" readonly is-link
            @click="show = true" />
        <van-popup v-model:show="show" position="bottom" round>
            <div class="cascader-wrap">
                <van-nav-bar title="选择类目" left-text="取消" right-text="确定" @click-left="onCancel"
                    @click-right="onConfirm" />
                <div class="popup-body">
                    <div v-if="loading" class="loading-wrap">
                        <van-skeleton animated :row="6" />
                    </div>
                    <van-empty v-else-if="!options || options.length === 0" description="暂无分类">
                        <van-button size="small" type="primary" @click="load">重试</van-button>
                    </van-empty>
                    <van-cascader v-else v-model="innerValue" :options="options"
                        :field-names="{ text: 'text', value: 'value', children: 'children' }" @change="onChange" />
                </div>
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
        modelValue: { type: [Number, String, null], default: null }, // categoryId
        label: { type: String, default: '分类' },
        placeholder: { type: String, default: '请选择分类' },
    },
    emits: ['update:modelValue', 'change'],
    setup(props, { emit }) {
        const show = ref(false)
        const options = ref([])
        const innerValue = ref(props.modelValue)
        const labelMap = ref(new Map())
        const loading = ref(false)

        watch(
            () => props.modelValue,
            (v) => {
                innerValue.value = v
            },
        )

        const selectedLabel = computed(() => labelMap.value.get(innerValue.value) || '')

        const buildTree = (flat) => {
            const nodes = new Map()
            // First pass: create nodes
            flat.forEach((c) => {
                const id = String(c.id)
                const parentId = c.parentId ? String(c.parentId) : null
                nodes.set(id, { text: c.name, value: c.id, parentId, children: [] })
                labelMap.value.set(c.id, c.name)
            })

            const roots = []
            nodes.forEach((node) => {
                if (node.parentId && nodes.has(node.parentId)) {
                    nodes.get(node.parentId).children.push(node)
                } else {
                    roots.push(node)
                }
            })

            // Optional: Clean up empty children arrays if needed, but Vant handles empty array as leaf.
            // However, to be safe against "blank tab" issues, we ensure leaf nodes have undefined children if that helps Vant?
            // Actually Vant Cascader: "If children is empty array, it is a leaf node." 
            // But if we moved to next tab, it implies children was NOT empty.

            return roots
        }

        const load = async () => {
            loading.value = true
            try {
                const res = await listCategories()
                options.value = buildTree(res)
            } catch (e) {
                console.error(e);
                options.value = [];
            } finally {
                loading.value = false
            }
        }

        const onChange = ({ value, selectedOptions }) => {
            // noop for now
        }

        const onCancel = () => {
            show.value = false
        }

        const onConfirm = () => {
            if (!innerValue.value) {
                // 延迟注册：避免直接引入 Toast 造成体积膨胀，交给外层校验
                return
            }
            emit('update:modelValue', innerValue.value)
            emit('change', innerValue.value)
            show.value = false
        }

        onMounted(load)

        return { show, options, innerValue, selectedLabel, onChange, onCancel, onConfirm, load, loading }
    },
}
</script>

<style scoped>
.cascader-wrap {
    height: 70vh;
    display: flex;
    flex-direction: column;
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
