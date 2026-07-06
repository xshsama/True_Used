<template>
    <div class="inspection-timeline">
        <h3>Platform Inspection Flow</h3>
        <div v-if="loading" class="loading">Loading...</div>
        <div v-else-if="!inspectionData" class="no-data">
            Inspection not started yet.
        </div>
        <div v-else class="timeline">
            <div class="summary-card" :class="inspectionData.status.toLowerCase()">
                <div class="status-header">
                    <span class="status-badge">{{ inspectionData.status }}</span>
                    <span class="time">{{ formatDate(inspectionData.updatedAt) }}</span>
                </div>
                <p class="summary-text" v-if="inspectionData.resultSummary">
                    {{ inspectionData.resultSummary }}
                </p>
            </div>

            <div class="timeline-items">
                <div v-for="item in inspectionData.items" :key="item.id" class="timeline-item"
                    :class="item.status.toLowerCase()">
                    <div class="timeline-marker"></div>
                    <div class="timeline-content">
                        <div class="item-header">
                            <span class="item-name">{{ item.itemName }}</span>
                            <span class="item-status">{{ item.status }}</span>
                        </div>
                        <p class="item-desc">{{ item.itemDescription }}</p>
                        <p v-if="item.notes" class="item-notes">
                            <strong>Notes:</strong> {{ item.notes }}
                        </p>
                        <div v-if="item.imageUrl" class="item-image">
                            <img :src="item.imageUrl" alt="Inspection Image" />
                        </div>
                        <div class="item-time" v-if="item.updatedAt">
                            {{ formatDate(item.updatedAt) }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { getInspectionFlow } from '@/api/inspection'
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
    orderId: {
        type: Number,
        required: true
    }
})

const loading = ref(false)
const inspectionData = ref(null)
let pollingInterval = null

const formatDate = (dateString) => {
    if (!dateString) return ''
    return new Date(dateString).toLocaleString()
}

const fetchInspectionData = async () => {
    try {
        const res = await getInspectionFlow(props.orderId)
        inspectionData.value = res.data

        // Stop polling if completed or failed
        if (res.data && (res.data.status === 'COMPLETED' || res.data.status === 'FAILED')) {
            stopPolling()
        }
    } catch (error) {
        console.error('Failed to fetch inspection data', error)
    } finally {
        loading.value = false
    }
}

const startPolling = () => {
    fetchInspectionData()
    pollingInterval = setInterval(fetchInspectionData, 3000) // Poll every 3 seconds
}

const stopPolling = () => {
    if (pollingInterval) {
        clearInterval(pollingInterval)
        pollingInterval = null
    }
}

watch(() => props.orderId, (newVal) => {
    if (newVal) {
        stopPolling()
        startPolling()
    }
})

onMounted(() => {
    startPolling()
})

onUnmounted(() => {
    stopPolling()
})

defineExpose({
    fetchInspectionData
})
</script>

<style scoped>
.inspection-timeline {
    padding: 20px;
    background: #f9f9f9;
    border-radius: 8px;
    margin-top: 20px;
}

.summary-card {
    padding: 15px;
    background: white;
    border-radius: 8px;
    margin-bottom: 20px;
    border-left: 5px solid #ccc;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.summary-card.completed {
    border-left-color: #4caf50;
}

.summary-card.in_progress {
    border-left-color: #2196f3;
}

.summary-card.failed {
    border-left-color: #f44336;
}

.status-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.status-badge {
    font-weight: bold;
    padding: 4px 8px;
    border-radius: 4px;
    background: #eee;
    font-size: 0.9em;
}

.completed .status-badge {
    background: #e8f5e9;
    color: #2e7d32;
}

.in_progress .status-badge {
    background: #e3f2fd;
    color: #1565c0;
}

.failed .status-badge {
    background: #ffebee;
    color: #c62828;
}

.timeline {
    position: relative;
    padding-left: 20px;
}

.timeline-items {
    border-left: 2px solid #ddd;
    padding-left: 20px;
}

.timeline-item {
    position: relative;
    margin-bottom: 20px;
}

.timeline-marker {
    position: absolute;
    left: -29px;
    /* Adjust based on padding/border */
    top: 5px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #fff;
    border: 3px solid #ddd;
    z-index: 1;
}

.timeline-item.passed .timeline-marker {
    border-color: #4caf50;
    background: #4caf50;
}

.timeline-item.failed .timeline-marker {
    border-color: #f44336;
    background: #f44336;
}

.timeline-item.pending .timeline-marker {
    border-color: #ddd;
}

.timeline-content {
    background: white;
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.item-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 5px;
}

.item-name {
    font-weight: bold;
}

.item-status {
    font-size: 0.85em;
    color: #666;
}

.item-desc {
    font-size: 0.9em;
    color: #555;
    margin-bottom: 5px;
}

.item-notes {
    font-size: 0.9em;
    color: #333;
    background: #fff8e1;
    padding: 5px;
    border-radius: 4px;
}

.item-time {
    font-size: 0.8em;
    color: #999;
    text-align: right;
    margin-top: 5px;
}
</style>
